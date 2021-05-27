# Java-multithreaded-search-project
In this part, we created a multithreaded search utility.
The utility will allow searching for all files with a specific extension in a root directory.
Files with the specific extension will be copied to a specified directory.
The application consists of two queues and three groups of threads.

Running and executing explain:
1. compile the java files via cmd or any IDE
2. RUN - java DiskSearcher <.boolean of milestoneQueueFlag> <.file-extension> <.root directory> <.destination directory> <# of searchers> <# of copiers>
3. an example: java DiskSearcher true txt C:\OS_Exercises C:\temp 10 5
