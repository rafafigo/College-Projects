/************************************************************************************************************/
/*								      	   Análise e Síntese de Algoritmos             						*/
/*									              First Project             	 						   	*/
/*                                                90719 - 90770                                             */
/*                                        Gonçalo Freire | Rafael Figueiredo                                */
/* **********************************************************************************************************/

#include <stdio.h>
#include <stdlib.h>

#define MIN(x, y) (((x) < (y)) ? (x) : (y))

/* Pointer to grid structure. */
typedef struct general *grid;

/* Pointer to auxiliar structure. */
typedef struct dfs_ex *dfs;

/* Pointer to linked list. */
typedef struct node *link; 

struct dfs_ex {
	_Bool bigId;
	_Bool erased;
	char nSons;
	char state;
	int visitTime;
	int lowTime;
	int pi;
	
};

struct general {
	int nRouters;
	int nConections;
	int nsubgrid;
	int time;
	int nArtic;
	dfs *extras;
	link *adj;

};

struct node { 
        int id; 
        link next; 
};


/* Declaration of Functions. */

void dfs_torjan(grid);

int visit_dfs_torjan(grid,int,int);

int dfs_max(grid);

int visit_dfs_max(grid, dfs, int, int);

link create_list();

link insertBegin(link, int);

void free_link(link);



int main() {
	int i=0;
	int adj1;
	int adj2;

	grid general = (grid) malloc(sizeof(struct general));

	/* Scan of the number of routers and Connections. */
	if (scanf("%d %d",&general->nRouters,&general->nConections) != 2 || general->nRouters < 2 || general->nConections < 1) {
		perror("Erro na leitura de inputs");
		exit(1);
	}
	/* Inicialization of general structure. */

	general->nsubgrid = 0;
	general->time = 0;
	general->nArtic = 0;
	general->extras = (dfs*) malloc((sizeof(dfs)*general->nRouters));
	general->adj = (link *) malloc((sizeof(link)*general->nRouters));

	for(; i<general->nRouters;i++) {
		general->extras[i] = (dfs) malloc(sizeof(struct dfs_ex));
		general->extras[i]->visitTime = 0;
		general->extras[i]->lowTime = 0;
		general->extras[i]->nSons = 0;
		general->extras[i]->state = 0;
		general->extras[i]->erased = 0;
		general->extras[i]->pi = 0;
		general->extras[i]->bigId = 0;
		general->adj[i] = NULL;
	}

	 /* Scan of all connections. */
	for(i=0; i < general->nConections; i++) {

		if(scanf("%d %d", &adj1, &adj2) != 2 || adj1<1 || adj1>general->nRouters 
													   || adj2<1 || adj2>general->nRouters) {
			perror("Erro na leitura de inputs");
			exit(1);
		}

		if(adj1 > general->nRouters || adj2 > general->nRouters) {
			perror("Erro na leitura de inputs");
			exit(1);
		}

		/* Insert the adjancencies into the linked list of each others. */
		general->adj[adj1-1] = insertBegin(general->adj[adj1-1], adj2);

		general->adj[adj2-1] = insertBegin(general->adj[adj2-1], adj1);

	}

	dfs_torjan(general);

	/* Output: */
	printf("%d\n",general->nsubgrid);

	/* Prints the bigId of each subrouter */
	for(i=0; i < general->nRouters ;i++) {

		if(general->extras[i]->bigId == 1){
			if ( --general->nsubgrid == 0) {
				printf("%d\n",i+1);
				break;
			}
		printf("%d ",i+1);
		}
	}

	printf("%d\n",general->nArtic);

	printf("%d\n",dfs_max(general));

	/* Free. */
	for(i=0; i<general->nRouters;i++){
		free_link(general->adj[i]);
		free(general->extras[i]);	
	}

	free(general->adj);
	free(general->extras);
	free(general);

	exit(0);
}

/* It does a dfs based on torjan algorithm to find all the articulation points and while doing  */
/* this we also get the number of articulations, the bigIds, and the number of subgrids.        */
void dfs_torjan(grid general) {
	int i=0;
	int bigId=0;
	dfs *extras = general->extras;
	
	for(;i<general->nRouters; i++) {
		
		/* verifies if we already visited this router , if we didn't that means its a new root. */
		if(extras[i]->state == 0) {
			general->nsubgrid++;
			/* Search all edges just like any dfs. */
			bigId = visit_dfs_torjan(general, i, i);
			general->extras[bigId]->bigId = 1;

			/* Condition: root has more than 1 son            */
			/* If true than the root is an articulation point.*/
			if(extras[i]->nSons > 1) {
				general->nArtic++;
				extras[i]->erased = 1;
			}
		}
	}
}


/* We go through the linked list of edges of the Vertex.                                                 */
/* If state == 1 (visited) then we only change minimum  of vertex lowTime and the edge visitTime.        */ 
/* If state == 2 (terminated) we ignore the edge.   										             */
/* If state == 0 (not visited) then we update the  number of sons (that is helpful for root), and search */ 
/* recursively throw all edges. After one of the vertexes terminates then we go back and update the      */
/* minimum of lowTime of both edge and vertexes, and check if it is an articulation point.               */ 

int visit_dfs_torjan(grid general, int vId, int bigId) {
	link edge=NULL;
	int eId = 0;
	dfs Vextras = general->extras[vId];
	dfs Eextras = NULL;

	Vextras->visitTime = ++general->time;
	Vextras->lowTime  = general->time;
	Vextras->state = 1; /* visited */
	Vextras->nSons = 0;

	for(edge=general->adj[vId]; edge!=NULL; edge=edge->next) {
		eId = edge->id-1;
		Eextras = general->extras[eId];
		
		if(Eextras->state == 0) {
			if(bigId < eId){
				bigId  = eId;
			}
			
			if(Vextras->nSons < 2)
				Vextras->nSons++;
			
			Eextras->pi = vId+1;
			
			bigId = visit_dfs_torjan(general, eId, bigId);
			
			Vextras->lowTime = MIN(Eextras->lowTime,Vextras->lowTime);
			
			/* Condition: not a root, not erased and edge visit time less than vertex low time  */
			/* If true then we have another articulation point.                                  */
			if(Vextras->erased == 0 && Vextras->pi != 0 && Vextras->visitTime <= Eextras->lowTime) {
				general->nArtic++;
				Vextras->erased = 1;
			}
		}
		else if(Eextras->state == 1 && Vextras->pi != eId+1){
			Vextras->lowTime = MIN(Vextras->lowTime,Eextras->visitTime);

		} 
	}
	Vextras->state = 2; /* terminated */
	return bigId;
}

/* Does another dfs but this time a very simple one, used only for searchig for */
/* the biggest subgrid after the articulation points being erased.              */
int dfs_max(grid general) {
	dfs *Vextras = general->extras;
	int max[2] = {0,0};
	int i=0;

	for(; i<general->nRouters; i++) {
		/* if the router is erased we ignore him. */
		if(Vextras[i]->erased == 1)
			continue;

		if(Vextras[i]->state == 2) {

			max[1] = visit_dfs_max(general, Vextras[i],0, i);
			if(max[0] < max[1]){
				max[0] = max[1];
			}
			
		}
	}
	return max[0];
}

/* State = 2 (not visited), State = 3 (visited), State = 4 (Terminated)                         */
/* If state = 2 then its a new vertex so we increment de number of vertexes of a subgrid (max)  */
/* If an edge is erased than that means it was an articulation point and now we ignore him      */
/* Just like the other visit we search recursivly for all edges.                                */
int visit_dfs_max(grid general, dfs Vextras, int max, int vId) {
	link edge = NULL;
	int eId = 0;
	dfs Eextras = NULL;

	if(Vextras->state == 2)
		max++;
	Vextras->state = 3;
	for(edge=general->adj[vId]; edge!=NULL; edge=edge->next) {
		eId = edge->id-1;
		Eextras = general->extras[eId];
		/* if the router is erased we ignore him. */
		if(Eextras->erased == 1) 
			continue;

		if(Eextras->state == 2) {
			max = visit_dfs_max(general, Eextras,max, eId);
		}
	}
	Vextras->state = 4;
	return max;
}


/* linked list */

/* Inserts new element at the begining of the linked list. */
link insertBegin(link head, int i) {
	link newEl = malloc(sizeof(struct node));

	newEl->id = i;
	newEl->next = head;
	return newEl;

}

/* Free of each element of the linked list. */
void free_link(link ls) {
	link p=NULL;
	while(ls!=NULL){
		p = ls->next;
		free(ls);
		ls = p;
	}
}