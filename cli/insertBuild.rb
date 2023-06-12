require 'optparse'
require 'fileutils'
require 'digest/sha2'

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
    option.on("--project ID") { |id|
      options[:project_id] = id
    }
    option.on("--projectFriendlyName NAME") { |name|
      options[:project_name] = name
    }
    option.on("--versionGroup GROUP") { |group|
      options[:group] = group
    }
    option.on("--version VERSION") { |version|
      options[:version] = version
    }
    option.on("--build BUILD") { |build|
      options[:build] = build
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
          puts "[HASH] Creating SHA256 hash for file '#{path}'"
          hash = Digest::SHA2.hexdigest(File.read(path))
          puts "[HASH] Hash: #{hash}"
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
  unless options.has_key?(:project_id)
    abort("[ERROR] Please specify a project ID using --project. Example: --project raper")
  end
  unless options.has_key?(:project_name)
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

# Main
options = parse_arguments
check_arguments options

downloads_path = File.join(options[:storage], options[:project_id], options[:version], options[:build])
puts "[FILES] Creating hosting directory: #{downloads_path}"
FileUtils.mkdir_p(downloads_path)

options[:downloads].each do |download|
  download_path = File.join(downloads_path, options[:project_id] + "-" + options[:version] + "-" + options[:build] + ".jar")
  puts "[FILES] Copying #{download.path} to #{download_path}"
  FileUtils.copy_file(download.path, download_path)
end