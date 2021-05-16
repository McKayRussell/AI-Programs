#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>        /* to write time */
#include <sys/types.h>   /* standard data types for systems programming */
#include <sys/file.h>    /* open() system call */
#include <unistd.h>      /* header file for the POSIX API */
#include <sys/wait.h>    /* for wait() system call */
#include <signal.h>		/* signal handling */
#include <errno.h>		/* for perror() call */

/** CTRL-C Signal Handler (10 points)
	The child PID and pipe file descriptor variables MUST be in global space so the signal
		handler function can access them. **/
pid_t cpid;
int pipefd[2];

/// Create the ctrlc_handler() function and have it do the following:
void ctrlc_handler(int signal){
	
	if(cpid == 0){
		/** If this is currently the child process, print out that the child is exiting and
			close the READ end of the pipe. **/
		printf("Child process exiting...\n");
		close(pipefd[0]);
		exit(0);
	}
	else {
		/** If this is currently the parent process, print out that the parent is exiting and
			close the WRITE end of the pipe. **/
		printf("Parent process exiting.\n");
		close(pipefd[1]);
		exit(0);
	}
}

int main(int argc, char *argv[]){
	int cStatus;
	char buf[30];
	
	/// At the top of main(), block all signals except SIGINT.
	sigset_t mask;
	sigfillset(&mask);
	sigdelset(&mask, SIGINT);
	sigprocmask(SIG_BLOCK, &mask, NULL);
	
	/// Set up the signal handler to call the ctrlc_handler() function when SIGINT is received.
	struct sigaction sa;
	sa.sa_handler = ctrlc_handler; /* Name of the signal handler function */
	sa.sa_flags = SA_RESTART;    /* restart system calls if interrupted */ 
	sigfillset(&sa.sa_mask);     /* mask all signals while in the handler */
	sigaction(SIGINT, &sa, NULL);
	
		/** Prior to Fork at the Top of main() (5 points) **/
	/// In addition to setting up the signal handler (above), print out the parent PID.
	printf("Parent ID is %d\n", getpid());
	
	/// Create an infinite loop, similar to Lab 10. Inside the loop, do the following:
	while(1){
		/// Set up the pipe using the pipe() system call.
		if(pipe(pipefd) < 0){	/* Call pipe() */
			perror("pipe: ");
			exit(1);
		}
		
		/// Call fork() to create the child process.
		cpid = fork();
		
		/// Follow the directions below for the child and parent processes.
		if(cpid == 0){
				/** Child Process Steps Inside the Infinite Loop (15 points) **/
			
			/// Close the WRITE end of the pipe.
			close(pipefd[1]);
			/// Read the command string from the pipe.
			read(pipefd[0], buf, sizeof(buf));
			
			printf("Received command '%s'\n", buf);
				
			/** Parse the command string as follows, using strcmp() to compare the command
					string to the supported commands: **/
			if(strcmp("quit", buf) == 0){
				/** If the command string is "quit", print that the child is exiting and exit
					with a positive number greater than 1 (your choice on this value). This
					will be the exit code to let the parent process know it needs to break out
					of the loop. **/
				printf("Child sending special exit code to parent.\n");
				exit(2);
			}
			else if(strcmp("list", buf) == 0){
				/** If the command string is "list", set up your new argument array (look at
					the examples for Lab 10) to call "/bin/ls" with the argument "-al". Then
					call execve() with the new argument array. **/
				char *listCmd[] = { "/bin/ls", "-al", ".", NULL }; 
				execve(listCmd[0], listCmd, NULL);
				exit(0);
			}
			else if(strcmp("cat", buf) == 0){
				/** If the command string is "cat", ask the user to input a filename and read
					that in to a string. Set up your new argument array to call "/bin/cat"
					with the filename as the argument. Then call execve() with the new
					argument array. **/
				printf("Enter filename: ");
				scanf("%s", buf);
				char *catCmd[] = { "/bin/cat", buf, NULL };
				execve(catCmd[0], catCmd, NULL);
				printf("\n");
				exit(0);
			}
			else {
				/** If any other string is entered, print out that it's an unrecognized 
					command and exit. **/
				printf("Command not recognized\n");
				exit(1);
			}
		}
		else {
				/** Parent Process Steps Inside the Infinite Loop (10 points) **/
			/// Close the READ end of the pipe.
			close(pipefd[0]);
			
			/// Print out the child PID.
			printf("Child PID is %d\n", cpid);
			
			/// Print out a prompt for the user to enter a command.
			printf("Enter command: ");
			
			/// Read the user's input into a string variable.
			scanf("%s", buf);
			
			/// Write that string variable to the write end of the pipe.
			write(pipefd[1], buf, sizeof(buf));
			
			/// Wait for the child to exit.
			wait(&cStatus);
			
			/// Close the WRITE end of the pipe once the child has exited.
			close(pipefd[1]);
			
			/// Print out the child's exit code.
			if(WIFEXITED(cStatus))
				printf("Child exited with exit code %d\n", WEXITSTATUS(cStatus));
			
			/** If the child exit code says the "quit" command was received by the child, break
				out of the loop and exit the program with success. **/
			if(WEXITSTATUS(cStatus) == 2){
				printf("Parent exiting.\n");
				exit(EXIT_SUCCESS);
			}
		}
	}
}