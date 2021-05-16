#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>			/* header file for the POSIX API */
#include <time.h>			/* to write time */
#include <sys/types.h>		/* standard data types for systems programming */
#include <sys/file.h>		/* file-related system call */
#include <sys/wait.h>		/* for wait() system call */
#include <signal.h>			/* signal handling */
#include <errno.h>			/* for perror() call */
#include <sys/ipc.h>		/* header file for SysV IPC */
#include <sys/shm.h>		/* shared memory calls */
#include <sys/msg.h>		/* message queue calls */
#include <pthread.h>		/* POSIX threads */
#include <sys/sem.h>		/* SysV semaphore header */
#include <stdint.h>			/* For 64 bit integers */
#include <sys/syscall.h>	/* Make a syscall() to retrieve our TID */

		/**	Global Variables and Command Line Argument (5 points) **/
/// Have a global LIMIT variable that is set to 5 by default.
int LIMIT = 5;
/** Have a global variable to indicate if the end of file has been reached. Initialize it to 0
		(not at end). **/
int endReached = 0;

/** Copy the fib() function from one of the labs to use as "busy work" during the producer and
		consumer threads. **/
int fib(int n) {
	if (n<2) return n;
	else return fib(n-1) + fib(n-2);
}

		/**	CTRL-C Signal Handler (5 points) **/
/** Make sure your message queue ID variable and semaphore ID variable are in global space so
		the signal handling function can see the variables. **/
/** Declare the message queue ID variable in global space so main(), ctrlc_handler(), the 
		producer thread, and the consumer threads can see it. **/
int mqID;
/** Declare the semaphore ID variable in global space so main(), ctrlc_handler(), the
		producer thread, and the consumer threads can see it. **/
int semID;

		/**	Message Queue Setup (5 points) **/
/** In global space, declare the structure for the message. In the lab, we made this just one
		variable shared between processes. In this solution, make it a template (e.g. typedef
		struct { ... } mq_msg;) and declare mq_msg mymsg variables in the producer and 
		consumer functions. **/
typedef struct {
	long type;
	char text[100];
} mq_msg;

		/**	Semaphores Setup and Initialization (10 points) **/
/// Copy the union semun variable used in the semaphore labs.
union semUnion {
	int val;
	struct semid_ds *buf;
	unsigned short *array;
	struct seminfo *__buf;
} my_semUnion;

/// Declare one "grab" action and one "release" action for the producer thread.
struct sembuf grabPsem[1];     /* Used to emulate semWait() */
struct sembuf releasePsem[1];  /* Used to emulate semSignal() */

/** Declare an array of 5 "grab" actions and another array of 5 "release" actions for the
		consumer threads (similar to the forks for the dining philosopher's lab). These
		should be declared to be consistent with your chosen semaphore type ("wait for n"
		or "wait for 0"). **/
struct sembuf grabCsem[5][1];     /* Used to emulate semWait() */
struct sembuf releaseCsem[5][1];  /* Used to emulate semSignal() */

		/**	Thread Setup, Spawn, and Join (5 points) **/
/** Create a global structure to pass the thread id number to the thread function, similar to
		Labs 6 and 9. **/
typedef struct{
	int tID;
 } thread_info;

		/** Create a ctrlc_handler() function that does the following: **/
void ctrlc_handler(int signal){
	
	printf("Cleaning up before terminating...\n");
	
	/// If the message queue is allocated, delete the message queue with msgctl().
	if(mqID >= 0)
		msgctl(mqID, IPC_RMID, NULL);
	
	/// If the semaphores are allocated, delete them using semctl().
	if(semID >= 0)
		semctl(semID, 0, IPC_RMID);
	
	/// Exit the program using exit(1), so all threads are also stopped.
	exit(1);
}

void *producer(void *dummy);
void *consumer(void *dummy);

int main(int argc, char *argv[]){
	
	/// At the start of main(), block all signals except SIGINT.
	sigset_t mask;
	sigfillset(&mask);
	sigdelset(&mask, SIGINT);
	sigprocmask(SIG_BLOCK, &mask, NULL);
	
	/** At the start of main(), set up a signal handler for SIGINT that calls the 
		ctrlc_handler() function. Note: Do not allow the signal handler to restart system
		calls, just in case your code has hung during semop(), msgrcv(), read(), or another
		system call. To do that, set sa_flags to 0 before calling sigaction(). **/
	struct sigaction sa;
	sa.sa_handler = ctrlc_handler; /* Name of the signal handler function */
	sa.sa_flags = 0;		/* Do not allow the signal handler to restart system calls */
	sigfillset(&sa.sa_mask);     /* mask all signals while in the handler */
	sigaction(SIGINT, &sa, NULL);
	
	/// As with Lab 8, if the user passes a command line argument, use that for LIMIT instead.
	if(argc > 1)
		LIMIT = atoi(argv[1]);
	
	/// In main(), create an IPC key from ftok().
	char pathname[256];
	getcwd(pathname, 256);
	strcat(pathname, "/foo");
	key_t ipckey = ftok(pathname, 12);
	
	/// In main(), before creating the threads, create the message queue with msgget().
	mqID = msgget(ipckey, IPC_CREAT | 0666);
	if(mqID < 0) {
		perror("IPC msgget: ");
		exit(1);
	}
	printf("Created message queue with ID: %d\n", mqID);
	
	/** Reuse the IPC key from the message queue to create a semaphore object with semget()
			that has 6 semaphores (5 for the consumers, plus 1 for the producer). The 5
			consumer semaphores will use semaphore indices 0-4. The producer semaphore will
			use semaphore index 5. **/
	int semNum = 6;
	semID = semget(ipckey, semNum, 0666 | IPC_CREAT);
	if(semID < 0){
		perror("semget: ");
		exit(1);
	}
	printf("Created semaphore with ID: %d\n", semID);
	
	/** Use semctl() to initialize the value for the 5 consumer semaphores (indices 0-4) and
			the producer semaphore (index 5) as appropriate for your chosen semaphore type
			("wait for n" or "wait for 0"). **/
	int i = 0;
	my_semUnion.val = 0;
	for(i = 0; i < 5; i++) {
		if(semctl(semID, i, SETVAL, my_semUnion) == -1) {
			perror("setting semaphore value: ");
			exit(1);
		}
		
		grabCsem[i][0].sem_num = i;
		grabCsem[i][0].sem_flg = SEM_UNDO;
		// The "grab" sem_op should be -1 for "wait for n"
		grabCsem[i][0].sem_op = -1;
		
		releaseCsem[i][0].sem_num = i;
		releaseCsem[i][0].sem_flg = SEM_UNDO;
		// The "release" sem_op should +1 for "wait for n"
		releaseCsem[i][0].sem_op = +1;
		
		printf("Consumer %d using semaphore %d\n", i, i);
	}
	my_semUnion.val = 1;
	if(semctl(semID, 5, SETVAL, my_semUnion) == -1) {
		perror("setting semaphore value: ");
		exit(1);
	}
	
	grabPsem[0].sem_num = 5;
	grabPsem[0].sem_flg = SEM_UNDO;
	// The "grab" sem_op should be -1 for "wait for n"
	grabPsem[0].sem_op = -1;
	
	releasePsem[0].sem_num = 5;
	releasePsem[0].sem_flg = SEM_UNDO;
	// The "release" sem_op should +1 for "wait for n"
	releasePsem[0].sem_op = +1;
	
	printf("Producer using semaphore 5\n");
	
	/** In main(), create an array of the thread id structures with 5 elements and have a loop
		to initialize each to the appropriate index number, e.g. data[i].myid = i. **/
	thread_info t_infoArray[5];
	for(i = 0; i < 5; i++){
		t_infoArray[i].tID = i;
	}
	
	/** Create a pthread_t array of 5 items for the consumer threads AND a second pthread_t
		variable for the producer thread. **/
	pthread_t conThreads[5];
	pthread_t proThread[1];
	
	int ret;
	/** Call pthread_create() for the producer function and pass it a dummy variable. (one
		producer thread created) **/
	int dummy = 12;
	printf("Calling pthread_create for producer thread...\n");
	ret = pthread_create(&proThread[0], NULL, producer, &dummy);
	if(ret) {
		perror("pthread_create: ");
		exit(EXIT_FAILURE);
	}
	
	/** Have a loop to call pthread_create() 5 times for the consumer function and pass each 
		their thread id structure, e.g. data[i]. (five consumer threads created) **/
	for(i = 0; i < 5; i++) {
		printf("Calling pthread_create for consumer thread %d...\n", i);
		ret = pthread_create(&conThreads[i], NULL, consumer, &t_infoArray[i]);
		if(ret) {
			perror("pthread_create: ");
			exit(EXIT_FAILURE);
		}
	}
	
	/// Call pthread_join() for the producer thread.
	printf("Calling pthread_join for producer thread...\n");
	if (pthread_join(proThread[0], NULL) < 0) {
		perror("pthread_join: ");
	}
	
	/// Have a loop to call pthread_join() 5 times for the consumer threads.
	for(i = 0; i < 5; i++){
		printf("Calling pthread_join for consumer thread %d...\n", i);
		if (pthread_join(conThreads[i], NULL) < 0) {
			perror("pthread_join: ");
		}
	}
	
	/// In main(), after joining the threads, delete the semaphore with semctl().
	if((semctl(semID, 0, IPC_RMID)) < 0) {
		perror("semctrl IPC_RMID:");
		exit(1);
	}
	
	/// In main(), after joining the threads, delete the message queue with msgctl().
	if(msgctl(mqID, IPC_RMID, NULL) < 0) {
		perror("msgctl: ");
		exit(1);
	}
}

void *producer(void *dummy){
	
			/**	Producer Thread Function (15 points) **/
	
	printf("Producer opening poem for reading.\n");
	/// Open the file "poem" for reading with a file stream (FILE *inf).
	FILE *infile = fopen("poem", "r");
	if(infile == NULL) {
		perror("fopen: ");
		exit(EXIT_FAILURE);
	}
	
	/** Create a local end of file variable and initialize it to 0. This will be set to 1 when
			it's time for the producer to exit and signal all of the consumer threads to also
			exit via the global "end of file" variable. **/
	int localEOF = 0;
	
	/** Create a local count of characters read off the file. This will be used purely
		internally by the producer to know when it's done reading the file. **/
	int charCount = 0;
	
	/** Create a local character buffer to read the data in from the file and a local mq_msg
			variable to pass that data to the message queue. **/
	char localBuf[100];
	mq_msg my_message;
	
	/// Print out the thread information: PID and system TID
	pid_t sysTID = syscall(SYS_gettid);
	printf("Producer thread pid: %d tid: %d.\n", getpid(), sysTID);
	
	int ret;
	/// Have a loop that starts at 0 continues while i < 5*LIMIT. The loop does the following:
	int i = 0;
	for(i = 0; i < 5*LIMIT; i++){
		/** If the local end of file variable is 1, set the global end of file variable to 1,
				call semop() with the "release" action for each of the consumer threads (5
				times), and break out of the loop. **/
		if(localEOF == 1){
			printf("End of file detected in loop %d.\n", i);
			endReached = 1;
			
			int j = 0;
			for(j = 0; j < 5; j++){
				printf("Signaling Consumer %d to exit.\n", j);
				ret = semop(semID, releaseCsem[j], 1);
				if(ret < 0) {
					perror("CSEM release: ");
					exit(1);
				}
			}
			break;
		}
		
		/** Reset the local count variable to 0 and use memset() to zero out the character
				buffer. **/
		charCount = 0;
		memset(localBuf, 0, 99);
		
		printf("Producer waiting on PSEM...\n");
		/// Call semop() with the producer's "grab" action to emulate semWait(PSEM).
		ret = semop(semID, grabPsem, 1);
		if(ret < 0) {
			perror("PSEM grab: ");
			exit(1);
		}
		
		/** Read a line from the file. To read a line from the file, keep using fgetc() until
				you either receive the '\n' character or you run out of space in your character
				buffer. Make sure to increment your local count variable after reading each
				character. If feof() is received, set the LOCAL end of file variable to 1, but
				not the global one (the next iteration of the producer loop will handle
				setting the global one through the above actions). The pseudocode for this is:
				|while local count is less than buffer size minus 1
				|	read fgetc() into buffer[count]
				|	if feof() set the local end of file variable and break
				|	if buffer[count] is '\n' break
				|	increment count **/
		
		printf("Producer reading from input file.\n");
		while(charCount < 99){
			localBuf[charCount] = fgetc(infile);
			if(feof(infile)){
				localEOF = 1;
				break;
			}
			if(localBuf[charCount] == '\n')
				break;
			charCount++;
		}
		
		/** Print out how many characters were read for the current line i, e.g. print out the
				local count. **/
		printf("Producer read %d characters from line %d.\n", charCount, i);
		
		/** If the local count is greater than 1, do the following: **/
		if(charCount > 1){
			/// Copy the buffer into mymsg.text and set mymsg.type appropriately.
			strcpy(my_message.text, localBuf);
			my_message.type = 1;
			
			/// Use msgsnd() to send the message.
			if(msgsnd(mqID, &my_message, sizeof(my_message), 0) == -1) {
				perror("IPC msgsnd: ");
			}
			
			/** Call fib(n) to emulate creating the data (use a sane value of n, such as 20 or
					30). **/
			fib(30);
			
			/** Signal consumer (i % 5) to read the message by calling semop() with the
					"release" variable for consumer (i % 5). This emulates
					semSignal(CSEM_(i%5)), e.g. semSignal(CSEM_0) or semSignal(CSEM_1). **/
			printf("Producer signaling Consumer %d to read line.\n", i % 5);
			ret = semop(semID, releaseCsem[i % 5], 1);
			if(ret < 0) {
				perror("CSEM release: ");
				exit(1);
			}
		}
	}
	/// Close the input file.
	fclose(infile);
	/// Use pthread_exit() to exit the thread.
	printf("Ending Producer loop.\n");
	pthread_exit(0);
}

void *consumer(void *dummy){
	
			/**	Consumer Thread Function (15 points) **/
			
	/// Declare a mq_msg mymsg variable to hold the data from the message queue.
	mq_msg my_message;
	
	/** Declare a local thread ID variable and use it to convert the passed void pointer back
			to a thread id. **/
	thread_info *localTID;
	localTID = (thread_info *)dummy;
	
	/// Print out the thread ID from the variable, the PID, and the system TID.
	pid_t sysTID = syscall(SYS_gettid);
	printf("Consumer %d thread pid: %d tid: %d.\n", localTID->tID, getpid(), sysTID);
	
	/** Declare a local filename variable with 256 characters (max filename on Odin). Use
			sprintf to create the string "log_offset%d" where %d is replaced by the thread ID
			from the passed variable, such as "log_offset0". **/
	char localFileName[256];
	sprintf(localFileName, "log_offset%d", localTID->tID);
	printf("Consumer %d opening log_offset%d for writing.\n", localTID->tID, localTID->tID);
	
	/// Open that filename for writing using file streams (FILE *outf).
	FILE *outfile = fopen(localFileName, "w");
    if(outfile == NULL) {
        perror("fopen: ");
        exit(EXIT_FAILURE);
    }
	
	int ret;
	/// Have a loop that starts at 0 continues while i < LIMIT. The loop does the following:
	int i = 0;
	for(i = 0; i < LIMIT; i++){
		/** Call semop() with the "grab" action for the current consumer thread. For example,
				if the thread ID from the variable is 1, this code would call semop() with the
				"grab" action for CSEM_1. Again, refer to the dining philosopher's lab to see
				how we did this with the forks. **/
		printf("Consumer %d waiting on CSEM %d...\n", localTID->tID, localTID->tID);
		ret = semop(semID, grabCsem[localTID->tID], 1);
		if(ret < 0) {
			perror("CSEM grab: ");
			exit(1);
		}
		
		/// If the global end of file variable is 1, break out of the loop.
		if(endReached == 1){
			printf("Consumer %d detected end of file signal from producer.\n", localTID->tID);
			break;
		}
		
		/// Retrieve the message from the message queue.
		printf("Consumer %d retrieving message from producer.\n", localTID->tID);
		ret = msgrcv(mqID, &my_message, sizeof(my_message), 0, 0);
		if(ret < 0) {
		   perror("IPC msgrcv: ");
		   exit(1);
		}
		
		/** Call fib(n) to emulate processing the data (use a sane value of n, such as 20 or
				30). **/
		fib(30);
		
		/** Call semop() with the producer's "release" action to signal it to read the next
				line. **/
		printf("Consumer %d signaling Producer.\n", localTID->tID);
		ret = semop(semID, releasePsem, 1);
		if(ret < 0) {
			perror("PSEM release: ");
			exit(1);
		}
		
		/// Write the retrieved message out to the file.
		fprintf(outfile, "%s", my_message.text);
	}
	
	/// Close the output file.
	fclose(outfile);
	///Use pthread_exit() to exit the thread.
	pthread_exit(0);
}
