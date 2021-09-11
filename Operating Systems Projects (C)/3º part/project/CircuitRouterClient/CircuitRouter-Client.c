/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * IST LEIC-T Sistemas Operativos 18/19
 * Exercicio 3 - CircuitRouter-Client.c
 *
 * Authors:
 * Miguel Levezinho  - 90756
 * Rafael Figueiredo - 90770
 */

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#define BUFF_SIZE 150

int main(int argc, char *argv[])
{
	int fifo_shell;   // Fifo from shell
	int fifo_client;  // Fifo from this client (unique)
	int status = 0;   // Exit status

	char dir_cli_path[BUFF_SIZE] = "/tmp/client_XXXXXX"; // path of the directory
	char cli_path[BUFF_SIZE]; // Client path

	char buffer[BUFF_SIZE];
	char buffer2[BUFF_SIZE];

	if (argc != 2)
	{
		perror("Invalid number of arguments");
		exit(1);
	}

	// Opens for writing in shell
	if ((fifo_shell = open(argv[1], O_WRONLY)) < 0)
	{
		perror("Error while opening fifo");
		exit(1);
	}
 
	mkdtemp(dir_cli_path); // Makes directory
	
	strcpy(cli_path, dir_cli_path);
	strcat(cli_path, "/"); 
	strcat(cli_path, &dir_cli_path[5]);

	// Creates client fifo with a unique path
	if (mkfifo(cli_path, 0777) < 0)
	{	 
		rmdir(dir_cli_path);
		perror("Error while creating fifo ");
		exit(1);
	}

	while (fgets(buffer, BUFF_SIZE, stdin) != NULL)
	{ 
		strcpy(buffer2, cli_path);
		strcat(buffer2, " ");
		strcat(buffer2, buffer); // Client path name + command

		// Size of BUFF_SIZE, for shell to receive exacly this number of characters
		// This prevents read from shell from reading another message at the time and alows select to work correctly
		if (write(fifo_shell, buffer2, BUFF_SIZE) <= 0)
		{
			perror("Error while sending to shell");
			status = 1;
			break;
		}

		/* Opens client fifo, with O_RDWR, to always have a writer associated
		 * By doing this we only block on read for a message (the read never returns 0 due to lost conection) */
		if ((fifo_client = open(cli_path, O_RDWR)) < 0) 
		{
			perror("Error while opening fifo");
			status = 1;
			break;
		}
		
		// Reads message sent by Shell or SeqSolver
		if (read(fifo_client, buffer, BUFF_SIZE) <= 0)
		{
			perror("Error while receiving from shell");
			close(fifo_client);
			status = 1;
			break;
		}
		
		// Print to stdout the message that was sent
		if (write(1, buffer, strlen(buffer) + 1) <= 0)
		{
			perror("Error while sending msg to stdin");
			close(fifo_client);
			status = 1;
			break;
		}
		close(fifo_client);
	}

	close(fifo_shell);
	unlink(cli_path);
	rmdir(dir_cli_path);
	exit(status);
}
