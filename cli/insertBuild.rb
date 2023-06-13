require 'optparse'
require 'fileutils'
require 'digest/sha2'
require 'rugged'
require 'mongo'

$standard_channels = %w[DEFAULT EXPERIMENTAL]
$standard_types = ["application"]

class Download

  attr_reader :type, :hash, :path

  def initialize(type, hash, path)
    @type = type
    @hash = hash
    @path = path
  end

end

def parse_arguments
  options = {}
  options[:downloads] = []
  OptionParser.new do |option|
    option.on("--projectName Name") { |name|
      options[:project_name] = name
    }
    option.on("--projectFriendlyName NAME") { |name|
      options[:project_friendly_name] = name
    }
    option.on("--versionGroup GROUP") { |group|
      options[:group] = group
    }
    option.on("--version VERSION") { |version|
      options[:version] = version
    }
    option.on("--build BUILD, AUTO") { |build|
      if build == "AUTO"
        options[:build_auto] = true
        options[:build] = nil
      else
        options[:build] = build
      end
    }
    option.on("--channel DEFAULT, EXPERIMENTAL") { |channel|
      unless $standard_channels.include?(channel)
        abort("[ERROR] Unknown channel: #{channel} please specify a valid channel#{$standard_channels}")
      end
      options[:channel] = channel
    }
    option.on("--repo PATH") { |path|
      options[:repo] = path
    }
    option.on("--storage PATH") { |path|
      options[:storage] = path
    }
    option.on("--download TYPE:PATH:HASH, TYPE:PATH") { |download|
      data = download.split(':')
      unless data.length >= 2
        abort("[ERROR] Invalid data length for download '#{data[0]}'")
      end

      type = data[0]
      unless $standard_types.include?(type)
        puts "[WARNING] You are not using[#{type}] standard types please consider using one of those: #{$standard_types}"
      end

      path = data[1]
      if File.exist?(path)
        if data.length > 2
          hash = data[2]
        else
          puts "[INFO/hash] Creating SHA256 hash for file '#{path}'"
          hash = Digest::SHA2.hexdigest(File.read(path))
          puts "[INFO/hash] Hash: #{hash}"
        end
      else
        abort("[ERROR] Could not find file '#{path}'")
      end

      options[:downloads].append(Download.new(type, hash, path))
    }
  end.parse!
  options
end

def check_arguments(options)
  unless options.has_key?(:project_name)
    abort("[ERROR] Please specify a project ID using --projectName. Example: --projectName raper")
  end
  unless options.has_key?(:project_friendly_name)
    abort("[ERROR] Please specify a project name using --projectFriendlyName. Example: --projectFriendlyName Raper")
  end
  unless options.has_key?(:group)
    abort("[ERROR] Please specify a version group using --versionGroup. Example: --versionGroup 1.20")
  end
  unless options.has_key?(:version)
    abort("[ERROR] Please specify a version using --version. Example: --version 1.20.1")
  end
  unless options.has_key?(:build)
    abort("[ERROR] Please specify a build number using --build. Example: --build 1")
  end
  unless options.has_key?(:channel)
    options[:channel] = "DEFAULT"
  end
  unless options.has_key?(:repo)
    abort("[ERROR] Please specify a repository path using --repo. Example: --repo raper/")
  end
  unless options.has_key?(:storage)
    abort("[ERROR] Please specify a storage path using --storage. Example: --storage /home/api/storage/")
  end
  if options[:downloads].length == 0
    abort("[ERROR] Please specify one or more downloads using --download. Example: --download application:paper-1.20.jar")
  end
end

def connect_to_mongodb
  puts "[INFO/database] Trying to establish connection to MongoDB Server..."
  url = "mongodb://127.0.0.1/bibliothek"
  if File.exist?("mongodb.url")
    url = File.read("mongodb.url")
  else
    puts "[INFO/database] No mongodb.url found using default #{url}"
  end
  Mongo::Client.new(url)
end

# Main
options = parse_arguments
build_number = options[:build].to_i
check_arguments options

client = connect_to_mongodb

puts "[INFO/database] Fetching and creating information..."
project = client[:projects].find_one_and_update(
  {
    "name": options[:project_name]
  },
  {
    "$setOnInsert": {
      "name": options[:project_name],
      "friendlyName": options[:project_friendly_name],
    }
  },
  :upsert => true,
  :return_document => :after
)
group = client[:groups].find_one_and_update(
  {
    "project": project["_id"],
    "name": options[:group]
  },
  {
    "$setOnInsert": {
      "project": project["_id"],
      "name": options[:group],
      "timestamp": Time.now
    }
  },
  :upsert => true,
  :return_document => :after
)
version = client[:versions].find_one_and_update(
  {
    "project": project["_id"],
    "name": options[:version]
  },
  {
    "$setOnInsert": {
      "project": project["_id"],
      "group": group["_id"],
      "name": options[:version],
      "timestamp": Time.now
    }
  },
  :upsert => true,
  :return_document => :after
)

puts "[INFO/build] Generating build number..."
previous_build = client[:builds].find(
  {
    "project": project["_id"],
    "version": version["_id"]
  },
  {
    "sort": {
      "_id": -1
    }
  }
).limit(1).first

if options.has_key?(:build_auto)
  if previous_build.nil?
    build_number = 1
    puts "[INFO/build] Next build number is 1"
  else
    build_number = previous_build["number"] + 1
    puts "[INFO/build] Next build number is #{build_number}"
  end
end

puts "[INFO/git] Loading git repo"
repo = Rugged::Repository.new(options[:repo])
commits = []

if !previous_build.nil? && previous_build[:changes].length > 0
  last_commit = repo.lookup(previous_build[:changes][0][:commit])
else
  last_commit = repo.head.target.parents.first
end

walker = Rugged::Walker.new(repo)
walker.sorting(Rugged::SORT_TOPO)
walker.push(last_commit)
walker.each do |commit|
  puts "[INFO/git] Commit: #{commit.message.strip!} # #{commit.oid}"

  commits << {
    "commit": commit.oid,
    "summary": commit.message,
    "message": commit.message
  }

  break if commit.oid == last_commit.oid
end

downloads = {}
options[:downloads].each do |download|
  downloads[download.type] = {
    "name": options[:project_name] + "-" + options[:version] + "-" + build_number.to_s + ".jar",
    "sha256": download.hash
  }
end

downloads_path = File.join(options[:storage], options[:project_name], options[:version], build_number.to_s)
puts "[INFO/files] Creating hosting directory: #{downloads_path}"
FileUtils.mkdir_p(downloads_path)

options[:downloads].each do |download|
  download_path = File.join(downloads_path, options[:project_name] + "-" + options[:version] + "-" + build_number.to_s + ".jar")
  puts "[INFO/files] Copying #{download.path} to #{download_path}"
  FileUtils.copy_file(download.path, download_path)
end

puts "[INFO/database] Adding build to database..."
client[:builds].insert_one(
  {
    "project": project["_id"],
    "version": version["_id"],
    "number": build_number,
    "timestamp": Time.now,
    "changes": commits,
    "downloads": downloads,
    "displayMode": "HIDE",
    "channel": options[:channel]
  }
)
puts "[FINISHED] Inserted build #{build_number.to_s} (Channel: #{options[:channel]}) for project #{options[:project_name]} (#{project["_id"]}) version #{options[:version]} (#{version["_id"]})"