const FileSystem = require("fs");
const Path = require("path");
const YArgs = require("yargs");
const GitLog = require("gitlog").default;
const {MongoClient} = require("mongodb");

const standardChannels = ["DEFAULT", "EXPERIMENTAL"];
const standardTypes = ["application"];

class Download {

    constructor(type, path, hash, name = null) {
        this.type = type;
        this.path = path;
        this.hash = hash;
        this.name = name;
    }

    valid() {
        return FileSystem.existsSync(this.path);
    }

}

function parseArguments() {
    return YArgs
        .option("projectName", buildOptions("p", "Example: raper"))
        .option("projectFriendlyName", buildOptions("n", "Example: Raper"))
        .option("versionGroup", buildOptions("g", "Example: 1.20"))
        .option("versionName", buildOptions("v", "Example: 1.20.1"))
        .option("buildNumber", buildOptions("b", "Example: -1 for AUTO or NUMBER", true, "number"))
        .option("repositoryPath", buildOptions("r", "Example: raper/"))
        .option("storagePath", buildOptions("s", "Example: storage/"))
        .option("download", buildOptions("d", "Example: application:buildJar.jar:hash:name"))
        .option("buildChannel", buildOptions("c", "Example: default or experimental", false))
        .default("buildChannel", "default")
        .help()
        .version(false)
        .argv;
}

function checkOptions(options) {
    options.buildChannel = options.buildChannel.toUpperCase(); // Build to upper case
    options.buildNumber = parseInt(options.buildNumber);

    if(!standardChannels.includes(options.buildChannel)) {
        console.error(`[ERROR] Unknown channel: "${options.buildChannel}" please specify a valid channel: ${standardChannels.join(", ")}`);
        process.exit(1);
    }

    if(typeof options.download === "string") {
        options.download = [options.download];
    }

    let primary = false;
    let downloads = [];
    options.download.forEach((download) => {
        const data = download.split(":");
        if(data.length >= 4) {
            download = new Download(data[0], data[1], data[2], data[3]);
        } else if(data.length === 3) {
            if(primary) {
                console.error("[ERROR] To many primary files, please check that!");
                process.exit(1);
            }

            download = new Download(data[0], data[1], data[2], null);
            primary = true;
        }
        if(!download.valid()) {
            console.error(`[ERROR] Invalid download found: ${download.type}`);
            download = null;
        }

        if(download) {
            console.log(`[INFO/download] Validated and added download ${download.type}`);
            downloads.push(download);
        }
    });
    if(downloads.length <= 0) {
        console.error("[ERROR] No downloads left. Quitting...");
        process.exit(1);
    }
    options.download = downloads;

}

function buildOptions(alias, description, required = true, type = "string") {
    return {
        alias: alias,
        describe: description,
        required: required,
        type: type
    }
}

async function writeOrGetProject(database, name, friendlyName) {
    return database.collection("projects").findOneAndUpdate(
        { name: name },
        {
          $setOnInsert: {
            name: name,
            friendlyName: friendlyName
          }
        },
        { upsert: true, returnOriginal: false }
    );
}

async function writeOrGetGroup(database, project, name) {
    return database.collection("groups").findOneAndUpdate(
        { project: project.value._id, name: name },
        {
          $setOnInsert: {
            project: project.value._id,
            name: name,
            timestamp: new Date()
          }
        },
        { upsert: true, returnOriginal: false }
    );
}

async function writeOrGetVersion(database, project, group, name) {
    return database.collection("versions").findOneAndUpdate(
        { project: project.value._id, name: name },
        {
          $setOnInsert: {
            project: project.value._id,
            group: group.value._id,
            name: name,
            timestamp: new Date()
          }
        },
        { upsert: true, returnOriginal: false }
    );
}

async function writeBuild(database, project, version, buildNumber, channel, changes, downloads) {
    return database.collection('builds').insertOne({
        project: project.value._id,
        version: version.value._id,
        number: buildNumber,
        timestamp: new Date(),
        changes: changes,
        downloads,
        displayMode: 'HIDE',
        channel: channel
    });
}

async function findLatestBuild(database, project, version) {
    return database.collection("builds").findOne(
        { project: project.value._id, version: version.value._id },
        { sort: { _id: -1 } }
    );
}

async function connectToMongoDB() {
    console.log("[INFO/database] Trying to establish connection to MongoDB Server...");

    let url = "mongodb://127.0.0.1/bibliothek";
    if(process.env.MONGODB_URL) {
        url = process.env.MONGODB_URL;
    } else if(FileSystem.existsSync("mongodb.url")) {
        url = FileSystem.readFileSync("mongodb.url", "utf-8");
    } else {
        console.log(`[INFO/database] No environment variable or mongodb.url file found. Using default: ${url}`);
    }

    const client = new MongoClient(url);

    try {
        await client.connect();
        return client;
    } catch(error) {
        console.error(`[ERROR/database] Failed to connect to MongoDB: ${error}`);
        process.exit(1);
    }
}

function buildStoragePath(storagePath, projectName, versionName, buildNumber) {
    return Path.join(storagePath, projectName, versionName, buildNumber.toString());
}

function generateName(download, projectName, versionName, buildNumber) {
    return download.name ? download.name : projectName + "-" + versionName + "-" + buildNumber + ".jar";
}

function copyDownload(download, storageFolder, projectName, versionName, buildNumber) {
    FileSystem.copyFileSync(download.path, Path.join(storageFolder, generateName(download, projectName, versionName, buildNumber)));
}

async function main() {
    const options = parseArguments();
    checkOptions(options);

    buildNumber = options.buildNumber;
    options.buildNumber = null; // Prevent usage

    const client = await connectToMongoDB();
    const database = client.db();

    console.log("[INFO/database] Fetching and creating information...");
    const project = await writeOrGetProject(database, options.projectName, options.projectFriendlyName);
    const group = await writeOrGetGroup(database, project, options.versionGroup);
    const version = await writeOrGetVersion(database, project, group, options.versionName);

    const previousBuild = await findLatestBuild(database, project, version);

    if(buildNumber === -1) {
        if(previousBuild) {
            buildNumber = previousBuild.number + 1;
        } else {
            buildNumber = 1;
        }
        console.log(`[INFO/build] Next build number is ${buildNumber}`);
    }

    // BuildNumber should be set for now. Ready to copy files

    // Copy files
    console.log("[INFO/copy] Copying files...");
    storagePath = buildStoragePath(options.storagePath, options.projectName, options.versionName, buildNumber);
    if(!FileSystem.existsSync(storagePath)) {
        FileSystem.mkdirSync(storagePath, { recursive: true });
    }
    options.download.forEach((download) => {
        copyDownload(download, storagePath, options.projectName, options.versionName, buildNumber);
    });
    console.log("[INFO/copy] Finished!");

    const lastBuildCommit = previousBuild && previousBuild.changes.length ? previousBuild.changes.slice(0, 1)[0].commit : "HEAD^1";

    // Get changes since last build
    let changes = [];
    GitLog(
        {
            repo: options.repositoryPath,
            fields: [ "hash", "subject", "rawBody" ],
            branch: lastBuildCommit + "...HEAD"
        }
    ).forEach((commit) => {
        changes.push(
            {
                commit: commit.hash,
                summary: commit.subject,
                message: commit.rawBody
            }
        );
        console.log(`[INFO/git] Commit: ${commit.subject} # ${commit.hash}`);
    });

    // Create downloads
    const downloads = {};
    options.download.forEach((download) => {
        downloads[download.type.replace(".", ":")] = {
            name: generateName(download, options.projectName, options.versionName, buildNumber),
            sha256: download.hash
        }
    });

    console.log("[INFO/database] Adding build to database...");
    await writeBuild(database, project, version, buildNumber, options.buildChannel, changes, downloads);

    await client.close();
    console.log(`[FINISHED] Inserted build ${buildNumber} (Channel: ${options.buildChannel}) for project ${options.projectName} (${project.value._id}) version ${options.versionName} (${version.value._id})`);
}

main().catch((error) => {
    console.error(`[ERROR] An unhandled exception occurred: ${error}`);
    process.exit(1);
});