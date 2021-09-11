/*************************************************************************************************************/
/*									Introdução a Algoritmos e Estrutura de dados							 */
/*									 Second Project - List (Doubly linked list)								 */
/*											   Rafael Alexandre 											 */
/*												    90770													 */
/* ***********************************************************************************************************/

#include "func.h"
/* Creates Doubly Linked list */
list create_list(){
	list i = NULL;

	if ((i =(list)malloc(sizeof(struct lis)))==NULL)
		perror("Error: Malloc");
	i->head = i->tail = NULL;
	return i;
}

/* Inserts new task at the end of the doubly linked list */
void insertEnd(list ls, task i){
	link new = (link)malloc(sizeof(struct node));

	new->tas = i;
	new->next = NULL;
	new->prev = ls->tail;
	if (ls->head == NULL){  /* If linked list is empty */
		ls->head = ls->tail = new;
		return;
	} 
	ls->tail->next = new;
	ls->tail = ls->tail->next; /* Tail gets the new task */ 

}

/* Remove the Task*/
void removeFirst(list ls, task i ){
	link ele_list=ls->head,aux=NULL;
	unsigned long id_re=get_task_id(i),id;

	if (ele_list->tas->id == i->id){ /* If id is head of doubly linked list*/
		aux = ele_list;
		ls->head = ele_list->next;	/*New head*/
		ele_list->next->prev = NULL;	/* Prev of new Head*/ 
		free_link(aux);
		return;
	}

	for(ele_list=ls->head; ele_list!=NULL;ele_list=ele_list->next){
		id = get_task_id(ele_list->tas);	/* Gets id of the task  */
		if (id == id_re){

			aux = ele_list;
			if (aux->next == NULL){		/* If the element is the tail */
				ele_list->prev->next = NULL;	/* Element is now null */
				ls->tail = ele_list->prev;		/* New tale */
				free_link(aux);
				break;
			}

			ele_list->prev->next = aux->next;	/* Updates the next position of the previous element */
			ele_list->next->prev = ele_list->prev; /* Uptates the prev postion of the next element*/
			free_link(aux);
			break;
		}
	}

}
/* Free of each element of the list */
void free_list(list ls){
	link i=NULL,p=NULL;
	i= ls->head;
	while(i!=NULL){
		p = i->next;
		free_link(i);
		i = p;
	}
	free(ls);
	
}
/* Free each link*/
void free_link(link ls){
	free_task(ls->tas);
	free(ls);
}

