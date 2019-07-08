/*************************************************************************************************************/
/*									Introdução a Algoritmos e Estrutura de dados							 */
/*											 Second Project	- Task			    							 */
/*											   Rafael Alexandre 											 */
/*												    90770													 */
/* ***********************************************************************************************************/

#include "func.h"
/* In this function I first check if the id and it's dependencies are valids and then I create the task */
task creates_task(unsigned long id ,char* description, unsigned long duration,unsigned long* ids, unsigned long len_prev, regulator reg_lis_has){
	int ver_id = verify_id_ids(id,ids,len_prev,reg_lis_has);	/* Verifies id and each dependencie */
	task tas=NULL;

	if ((tas = (task) malloc(sizeof(struct tas)))==NULL)
		perror("Erro:Malloc");

 	if (ver_id == 0){ 
 		free(ids);
 		free(description);
 		free(tas);
 		return tas = NULL;
 	}

	tas->id = id;
	tas->description = description;
	tas->duration = duration;
	tas->len_prev = len_prev;
	tas->len_next = 0;
	tas->init = 0;
	tas->task_prev = search_dependences(ids,reg_lis_has,len_prev);	/* Get's dependencies */
	tas->task_next = NULL;
	tas->early_start = 0;
	tas->late_start = 0;

	insert_hash_list(tas,reg_lis_has); /* Insert the task on Hashtable */
	add_dep(tas->task_prev,tas,len_prev); /* Adds Dependents */

	return tas;
}

/* Gets tasks of all the ids (dependencies) (prev) */
task *search_dependences(unsigned long *ids,regulator reg_lis_has, unsigned long len_prev){
	unsigned long i=0;
	task *tas=NULL;

	if (len_prev == 0){
		return tas;          /* No dependencies */
	}

	if ((tas = (task*) malloc(sizeof(task)*len_prev))==NULL)
		perror("Error: Malloc");

	while(i<len_prev){
		tas[i] = search(ids[i], reg_lis_has);       /* Get's dependencies by searching on the Hashtable*/
		i++;
	}

	free(ids);
	return tas;

}

/* Adds in each dependency the task that we are adding in each dependents(Next) and increases len of dependents*/
void add_dep(task *tasks, task tas, unsigned long len_prev){
	unsigned long i=0,p;

	if(len_prev == 0){           /* No dependencies */
		return;
	}

	while(i<len_prev){
		p = tasks[i]->len_next;

		if(p == 0){
			if((tasks[i]->task_next = (task*) malloc(sizeof(task)*(p+1)))==NULL)
				perror("Error: Malloc");
		}
		else
			if((tasks[i]->task_next = (task*)realloc(tasks[i]->task_next,sizeof(task) * (p+1)))==NULL)
				perror("Error: Realloc");

		tasks[i]->task_next[p] = tas;    /* Put's new dependent */
		tasks[i]->len_next += 1;
		i++;
	}

}

unsigned long get_task_id(task tas){
	return tas->id;
}

/* Prints the outputs of Duration and Path*/
void show_task(task tas, int path, unsigned long duration){
	if(tas->duration < duration)
		return;
	if (path == 0 || path == 1)		/* Duration */
		printf("%lu \"%s\" %lu",tas->id,tas->description,tas->duration);
	if (path == 1){		/* If path is active */
		if(tas->early_start == tas->late_start)
			printf(" [%lu CRITICAL]",tas->early_start);
		else
			printf(" [%lu %lu]",tas->early_start,tas->late_start);
	}
	if (path == 2){   /* Path */   
		if (tas->early_start == tas->late_start){
			printf("%lu \"%s\" %lu",tas->id,tas->description,tas->duration);
			printf(" [%lu CRITICAL]",tas->early_start);
		}
		else
			return;
	}
	show_ids(tas->task_prev,tas->len_prev);
}

/* Prints Dependencies for path and duration */
void show_ids(task * tass, unsigned long len){
	unsigned long i=0;
	if (len == 0){		/* No dependencies */
		printf("\n");
		return;
	}

	while(i<len){
		printf(" %lu",tass[i]->id); /* Prints dependencies */
		i++;
	}
	printf("\n");
}

/* Prints dependents for Depend */
void print_dependents(task item){
	unsigned long len_dep,i=0,id;
	task *tas;

	len_dep = item->len_next; 
	tas = item->task_next;

	while(i<len_dep){
		id = tas[i]->id;
		printf(" %lu",id);  /*Prints dependents */
		i++;
	}
	printf("\n");
}

/* Removes dependent (from next) from each dependency (prev of task that we are removing) */
void remove_ant(task tas){
	unsigned long len = tas->len_prev,i=0,p,len_t,change = 0;

	while(i<(len)){
		len_t = tas->task_prev[i]->len_next; /* Gets len of the task that we are removing */
		change = 0;                          /* Initialize variable that tell us if we found the task that we are removing*/

		for(p=0; p<(len_t-1);p++){
			if (tas->task_prev[i]->task_next[p]->id == tas->id)       /* Found the task */
				change = 1;
			if(change == 1){
				tas->task_prev[i]->task_next[p] = tas->task_prev[i]->task_next[p+1]; /* Removes Task */
			}
		}
		if (len_t !=0){
			tas->task_prev[i]->len_next -=1; /* Decreces the size */
			if((tas->task_prev[i]->task_next = (task*)realloc(tas->task_prev[i]->task_next,sizeof(task) * (p+1))) == NULL)
				perror("Erro:Realloc");
		i++;
		}
	}
		
}

/* Frees task */
void free_task(task tas){
	free(tas->description);
	free(tas->task_prev);
	free(tas->task_next);
	free(tas);
}