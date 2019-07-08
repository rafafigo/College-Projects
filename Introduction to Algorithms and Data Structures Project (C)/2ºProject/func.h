/************************************************************************************************************/
/*									Introdução a Algoritmos e Estrutura de dados							*/
/*								Second Project	- Func (Declarations and Structures)					   	*/
/*											   Rafael Alexandre 											*/
/*												    90770													*/
/* **********************************************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifndef Task_h
#define Task_h

/* Structure of Task */
typedef struct tas *task;

struct tas {
	unsigned long id;
	char *description;
	unsigned long duration;
	unsigned long early_start;
	unsigned long late_start;
	unsigned long len_prev;
	unsigned long len_next;
	int init;
	task *task_prev;
	task *task_next;
};

/* Doubly linked list */
typedef struct node *link; 

struct node { 
        task tas; 
        link next; 
        link prev; 
};

/* Head and Tail of doubly linked list */
typedef struct lis *list;

struct lis{ 
     link head; 
     link tail; 
}; 

/* Structure of Hashtable (with linked list) */
typedef struct hashtable *Hashtable;

struct hashtable{
	task item;
	Hashtable next;
};

/* Regulator */
typedef struct reg *regulator;

struct reg{
	list task_l;
	Hashtable *heads;
	unsigned long size_Hash;
};


#endif

/* Main Functions */
int add_task(regulator,int);

void duration_task(regulator,int );

void depend_task(regulator);

int remove_task(regulator,int);

int path(regulator);

void put_critical_zero(regulator);

void exit_free(regulator);

/* ------- */

/* Task Functions */
task creates_task(unsigned long ,char*,unsigned long,unsigned long*,unsigned long, regulator);

task *search_dependences(unsigned long *, regulator, unsigned long);

void add_dep(task *, task, unsigned long );

unsigned long get_task_id(task );

void show_task(task,int,unsigned long );

void show_ids(task *, unsigned long);

void print_dependents(task item);

void free_task(task);

void remove_ant(task);

/*-------------------*/

/* Hashtable Functions */
Hashtable* init_has_(unsigned long);

unsigned long hash(unsigned long,unsigned long );

void insert_hash_list(task,regulator);

void insertBegin(unsigned long key, task,regulator);

task search(unsigned long, regulator);

void delete_has(task,regulator);

void remove_task_has(unsigned long, task,Hashtable *);

void change_size_list(regulator );

int verify_id_ids(unsigned long, unsigned long *,unsigned long,regulator);

int verify_id(unsigned long,regulator);

void free_hash(regulator);

void free_h(Hashtable);

/* --------------------*/

/* List Functions */
list create_list();

void insertEnd(list, task);

void removeFirst(list, task);

void free_link(link);

void free_list(list);
