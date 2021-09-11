/*************************************************************************************************************/
/*									Introdução a Algoritmos e Estrutura de dados							 */
/*											  Second Project - Main			    							 */
/*											   	Rafael Alexandre 											 */
/*												    90770													 */
/* ***********************************************************************************************************/

#include "func.h"
#define N_Hash 1000

int main(){
	char *command =NULL;
	int com =1, path_act = 0;
	regulator reg_lis_has;

	if((command = (char* ) malloc(sizeof(char)*9)) == NULL)
		perror("Error: Malloc");
	if ((reg_lis_has =(regulator)malloc(sizeof(struct reg)))==NULL)
		perror("Error: Malloc");

	reg_lis_has->task_l = create_list(); /* Gets doubly linked list */
	reg_lis_has->heads =  init_has_(N_Hash); /* Gets Hastable (with linked list) */
	reg_lis_has->size_Hash = N_Hash; /* Gets initial size of hashtable */

	while(com!=0){
		if (scanf("%s",command)!=1){
			scanf("%*[^\n]");
			printf("illegal arguments\n");
			return 0;
		}

		else if (strcmp(command,"add")==0){
			path_act = add_task(reg_lis_has,path_act); 
		}
		else if(strcmp(command,"duration")==0){
			duration_task(reg_lis_has, path_act); 
		}

		else if(strcmp(command,"depend")==0){
			depend_task(reg_lis_has); 
		}

		else if(strcmp(command,"remove")==0){
			path_act = remove_task(reg_lis_has,path_act);
		}

		else if(strcmp(command,"path")==0){
			path_act = path(reg_lis_has);
		}

		else if(strcmp(command,"exit")==0){
			exit_free(reg_lis_has);
			free(command);
			com=0;
		}
		else{
			scanf("%*[^\n]");
			printf("illegal arguments\n");
		}
	}
	return 0;
}

/*Creates task */
int add_task(regulator reg_lis_has, int path_act){
	task tas=NULL;
	int var_id = 0;
	unsigned long *ids=NULL,id=0,duration,i=0;
	char space, desc[8001],*description;

	if(scanf("%lu \"%8000[^\"]\" %lu",&id,desc,&duration)!=3 || id==0 || duration==0){
		scanf("%*[^\n]");
		printf("illegal arguments\n");
		return path_act;
	}
	if((description = (char*)malloc(sizeof(char)*(strlen(desc)+1)))==NULL)
		perror("Error: Malloc");

	strcpy(description,desc);

	space = getchar();
	if (space == ' '){		/* Has ids */
		if((ids = (unsigned long*) malloc(sizeof(unsigned long)*10))==NULL)
			perror("Error: Malloc");

		for(i=0;space == ' ' ;i++){ /* Until /n gets ids */
			var_id = 1;

			if (scanf("%lu",&ids[i])!=1){ 
				scanf("%*[^\n]");
				free(description);
				free(ids);
				printf("illegal arguments\n");
				return path_act;
			}

			if (i%10 == 0)
				if ((ids = (unsigned long*) realloc(ids,sizeof(unsigned long) * (i+10))) == NULL)
					perror("Error:realloc");

			space = getchar();
		}
	}

	else if (space == '\n'){
		tas = creates_task(id,description,duration,ids,0,reg_lis_has); /* Creates task with no ids */
	}

	if (var_id == 1){
		tas = creates_task(id,description,duration,ids,i,reg_lis_has); /* Creates task with ids */
	}

	if (tas == NULL){
		return path_act;
	}

	insertEnd(reg_lis_has->task_l,tas); /* Inserts the element in the doubly linked list */
	if (path_act == 1) 
		put_critical_zero(reg_lis_has); /* Desactivate path put all criticals to zero */
	return 0;
}

/* Gets duration */
void duration_task(regulator reg_lis_has, int path_act ){
	long unsigned duration=0;
	link i;
	char c;

	c = getchar();
	if (c == ' '){
		if (scanf("%lu",&duration)!=1 || duration == 0){
			scanf("%*[^\n]");
			printf("illegal arguments\n");
			return;
		}
	}
	                   
	for (i=reg_lis_has->task_l->head; i!=NULL;i=i->next){
		show_task(i->tas,path_act,duration); /* prints duration of task */
	}
}

/* Prints id and all it's dependents*/
void depend_task(regulator reg_lis_has){
	task item=NULL;
	int exist_id = 0;
	unsigned long id;
	char c;

	c = getchar();
	if (c != ' '){
		printf("illegal arguments\n");
		return;
	}

	if (scanf("%lu",&id)!=1 || id == 0){
		scanf("%*[^\n]");
		printf("illegal arguments\n");
		return;
	}

	exist_id = verify_id(id,reg_lis_has); /* See if the id exists */

	if (exist_id ==1){  
		scanf("%*[^\n]");
		printf("no such task\n");
		return;
	}

	item = search(id,reg_lis_has); /* Gets the item from hashtable */

	if (item->len_next == 0){ /* See if have dependents (next) */
		printf("%lu: no dependencies\n",item->id);
		return;
	}

	else{
		printf("%lu:",item->id);
		print_dependents(item); /* Prints dependents (next) */
	}
}

/* Removes task - deletes the task from linked list (Hashtable) and from doubly linked list*/
int remove_task(regulator reg_lis_has,int path_act){
	task item=NULL;
	int exist_id = 0;
	unsigned long id;

	if (scanf("%lu",&id)!=1 || id == 0){
		scanf("%*[^\n]");
		printf("illegal arguments\n");
		return path_act;
	}

	exist_id = verify_id(id,reg_lis_has); /* Verify if the id exists */

	if (exist_id ==1){
		scanf("%*[^\n]");
		printf("no such task\n");
		return path_act;
	}

	item = search(id,reg_lis_has); /* Gets the item */

	if (item->len_next != 0){ /* See if exists dependents (next) */
		printf("task with dependencies\n"); 
		return path_act;
	}
	else{
		remove_ant(item);      /*Remove from dependents (next) of the dependencies (prev) task */
		delete_has(item,reg_lis_has); /* Deletes task in the hashtable */
		removeFirst(reg_lis_has->task_l,item); /* Deletes element from doubly linked list */
		if (path_act == 1){
			put_critical_zero(reg_lis_has); /* If path is active delete all early starts and late starts */
		}
		return 0;
	}
}

/* Path finds the early start, late start and duration of the project */
int path(regulator reg_lis_has){
	link p_l=NULL; /* p_l equal to doubly linked list */
	task *i=NULL;
	unsigned long p,project =0, path_act = 2;

	for(p_l=reg_lis_has->task_l->head ;p_l!=NULL ;p_l=p_l->next ){ /* Passes through all the tasks in the doubly linked list from head to tail */
		for(p=0,i=p_l->tas->task_next ;p<p_l->tas->len_next ;p++){ /* Passes through all the dependents of the task (next) */
			if (i[p]->early_start < (p_l->tas->duration+p_l ->tas->early_start)) /* Changes the early start if its bigger than what is already there */
				i[p]->early_start = p_l->tas->duration + p_l->tas->early_start;
		}

		if(p_l->tas->len_next == 0 && project < p_l->tas->early_start + p_l->tas->duration){ /* Get's the bigger early start */ 
			project = p_l->tas->early_start + p_l->tas->duration;   /* Duration of the project */
		}
	}

	for(p_l=reg_lis_has->task_l->tail ;p_l!=NULL ;p_l=p_l->prev ){  /* Passes through all the tasks in the doubly linked list from tail to head */
		if(p_l->tas->len_next == 0){
			p_l->tas->late_start = project - p_l->tas->duration; /* Gets the late start of the tasks with no dependents */
		}

		for(p=0,i=p_l->tas->task_prev ; p<p_l ->tas->len_prev ;p++){ /* Passes through all the dependencies (prev) of the task */ 
			if (i[p]->late_start == 0 && i[p]->init==0){             /* See if the init is off if it is and late star equal to zero then gets the late start */
				i[p]->init =1;
				i[p]->late_start = p_l->tas->late_start - p[i]->duration; /* Changes late start */
			}

			if (i[p]->late_start > (p_l->tas->late_start-p[i]->duration)){ /* if the new late start is less than what is already there then we need to change it */
				i[p]->late_start = p_l->tas->late_start -p[i]->duration;
			}
		}
	}

	for(p_l=reg_lis_has->task_l->head ;p_l!=NULL ;p_l=p_l->next )
		show_task(p_l->tas,path_act,0);                                   /* Prints path of the ones that are critical */
	printf("project duration = %lu\n",project);
	return 1;
}

/* Puts all early starts and all late starts to zero, and desactivate init */
void put_critical_zero(regulator reg_lis_has){
	link i=NULL;
	for(i=reg_lis_has->task_l->head ;i!=NULL ;i=i->next ){
		i->tas->init = 0;
		i->tas->late_start =0;
		i->tas->early_start = 0;
	}
}

/* Frees all elements from the regulator */
void exit_free(regulator reg_lis_has){
	free_list(reg_lis_has->task_l);
	free_hash(reg_lis_has);
	free(reg_lis_has);
}
