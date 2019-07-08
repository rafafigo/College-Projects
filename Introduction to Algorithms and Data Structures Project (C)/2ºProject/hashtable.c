/*************************************************************************************************************/
/*									Introdução a Algoritmos e Estrutura de dados							 */
/*									Second Project - Hashtable (with Linked list)							 */
/*											    Rafael Alexandre 											 */
/*												    90770													 */
/* ***********************************************************************************************************/

#include "func.h"

/* Initialize the Hashtable and it's size */
Hashtable *init_has_(unsigned long size_Hash){
	unsigned long i;
	Hashtable *heads;
	if((heads = (Hashtable*)malloc(size_Hash*sizeof(struct hashtable)))==NULL)
		perror("Error:Malloc");

	for(i=0; i<size_Hash;i++)
		heads[i] = NULL;
	return heads;
}

/* Get's the key */
unsigned long hash(unsigned long id, unsigned long size_Hash){
	unsigned long t = id % size_Hash;
	return t;
}

/* Inserts at the begining the new task */
void insert_hash_list(task tas,regulator reg_lis_has){
	unsigned long id = tas->id; 
	unsigned long i = hash(id,reg_lis_has->size_Hash); /* Finds the key */
	insertBegin(i,tas,reg_lis_has);

}

/* Changes the head in the key  */
void insertBegin(unsigned long key, task tas,regulator reg_lis_has){
	Hashtable new, t  = reg_lis_has->heads[key]; 
	if((new = (Hashtable)malloc(sizeof(struct hashtable)))==NULL)
		perror("Erro Malloc");

	new->item = tas;
	new->next = NULL;
	if (reg_lis_has->heads[key] == NULL){ /*If head has no elements NULL */
		reg_lis_has->heads[key] = new;
		return;
	}
	new->next= t;
	reg_lis_has->heads[key] = new;

} 

/* Finds the elements*/
task search(unsigned long id,regulator reg_lis_has){
	unsigned long key=hash(id,reg_lis_has->size_Hash), N;
	double size_list;
	Hashtable c = NULL;

	for(c=reg_lis_has->heads[key],N=0; c!= NULL;c= c->next,N++){
		if(c->item->id == id){		/*Found the item */
			break;
		}
	}

	size_list = N/reg_lis_has->size_Hash;   /* If size list > 0.5 than it takes too long, needs to be resized*/

	if (size_list >0.5){
		change_size_list(reg_lis_has);

		key=hash(id,reg_lis_has->size_Hash);  /* Finds new key */
		for(c=reg_lis_has->heads[key],N=0; c!= NULL;c= c->next,N++){
			if(c->item->id == id){		/*Found the item in the new hashtable */
				break;
			}
		}
	}

	return c->item;
}

/* Finds key and calls the function to remove task from hashtable */
void delete_has(task tas, regulator reg_lis_has){
	unsigned long id = tas->id;
	unsigned long i = hash(id,reg_lis_has->size_Hash);
	remove_task_has(i, tas,reg_lis_has->heads);
}

/* Removes task from hastable */
void remove_task_has(unsigned long key, task tas,Hashtable *heads){
	Hashtable t=NULL,x = NULL;
	t = heads[key];

	if (t->next == NULL){ /* If head has only one element */
		x = t;
		heads[key] = t->next;
		free(x);
		return;
	}

	for(t=heads[key]; t->next!=NULL;t=t->next){
		if (t->next->item == tas){;      /* Deletes task from linked list (of Hashtable) */
			x = t->next;
			t->next = x->next;
			free(x);
			break;
		}
	}
}	

/* Changes size of the list */
void change_size_list(regulator reg_lis_has){
	link c=NULL;

	free_hash(reg_lis_has);
	reg_lis_has->size_Hash *= 2; /* Doubles the size of hashtable */
	reg_lis_has->heads = init_has_(reg_lis_has->size_Hash); /* Creates new hashtable */

	for (c=reg_lis_has->task_l->head; c!=NULL;c=c->next){
		insert_hash_list(c->tas,reg_lis_has);
	}
}

/* Check if id and ids exist to add new task */
int verify_id_ids(unsigned long id, unsigned long *ids, unsigned long len,regulator reg_lis_has) { 
	unsigned long key=hash(id,reg_lis_has->size_Hash),i=0,p;
	int found =0;
	Hashtable c=NULL;

	for(c=reg_lis_has->heads[key]; c!=NULL; c = c->next){
		if (reg_lis_has->heads[key]->item->id == id){ /* Found the id */
			printf("id already exists\n"); 
			return 0;
		}
	}

	if (ids==NULL){
		return 1; /* Don't have dependencies */
	}

	while(i<len){ /* See if all the dependencies exist */
		key = hash(ids[i],reg_lis_has->size_Hash);

		for(p=0,c=reg_lis_has->heads[key],found=0; c!= NULL; c = c->next,p++){
			if (c->item->id == ids[i]){
				found = 1;
				break;
			}
		}
		if (found==1)
			i++;
		else{
			printf("no such task\n");
			return 0;
		}
	}	

	return 1;	

}

/* See if the element exists */
int verify_id(unsigned long id, regulator reg_lis_has){
	unsigned long key=hash(id,reg_lis_has->size_Hash);
	Hashtable c=NULL;

	for(c=reg_lis_has->heads[key]; c!=NULL; c = c->next){
		if (c->item->id == id){
			return 0;
		}
	}
	return 1;
}


/* Frees the heads of hashtable and every element of the linked list */
void free_hash(regulator reg_lis_has){
	Hashtable head;
	unsigned long s=0;
	head = reg_lis_has->heads[s];
	while(s<reg_lis_has->size_Hash){
			free_h(head); /* Free elements of linked list */
		s++;
		head = reg_lis_has->heads[s];
	}
	free(reg_lis_has->heads);
	
}

/* Frees linked list */
void free_h(Hashtable link){
	Hashtable l_next = NULL;

	while (link!=NULL){
		l_next = link->next;
		free(link);
		link = l_next;
	}
}

