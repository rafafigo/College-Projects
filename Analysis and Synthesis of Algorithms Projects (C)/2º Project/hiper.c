/************************************************************************************************************/
/*								      	   Análise e Síntese de Algoritmos             						*/
/*									               Second Project             	 						   	*/
/*                                                90719  -   90770                                          */
/*                                        Gonçalo Freire | Rafael Figueiredo                                */
/* **********************************************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#define MIN(x, y) (((x) < (y)) ? (x) : (y))

typedef struct grid *graph; 

/* vert: Pointer to vertex structure. */
/* edge: Pointer to edge structure.   */
typedef struct v *vert;
typedef struct e *edge;

/*list_e: Pointer to a list that contains head and tail to a node of edge.  */
/*list_v: Pointer to a list that contains head and tail to a node of vertex.*/
typedef struct slist_e *list_e;
typedef struct slist_v *list_v;

/*link_e: Pointer to a node  of edge.  */
/*link_v: Pointer to a node of vertex. */
typedef struct node_adj  *link_e;
typedef struct node_vert *link_v;

/*struct grid:                                                  */
/* nVertices: Keeps the number of vertex's of graph.            */
/* queue: Linked list of nodes of vertex's that have excess.    */
/* heights: Vector with a linked list of vertex's in each index.*/
struct grid {
    int nVertices;
    vert *vertex;
    list_v queue;
    link_v *heights;
};

/*struct v:                                                  */
/* height: Keeps the actual height of the vertex.            */
/* excess: Keeps the excess flow on the vertex.              */
/* id: Id of the vertex.                                     */
/* adj Pointer to a linked list of adjacencies.              */
struct v {   
    int height;
    int excess;
    int id;
    list_e adj;
};

/*struct e:                                                  */
/* capacity: Keeps the capacity of the edge.                 */
/* flow: Keeps the actual flow of the vertex at the moment.  */
/* out: Pointer to destiny of the edge.                      */
struct e {
    int capacity;
    int flow;
    vert out;
};

/*struct slist_v: Used in the Queue (fifo)                   */
/* head: Pointer to first element of vertex nodes.           */
/* tail: Pointer to last element of vertex nodes.            */
struct slist_v {
    link_v head;
    link_v tail;
};

/*struct slist_e: Used in adajencies.                       */
/* head: Pointer to first element of edge  nodes.           */
/* tail: Pointer to last element of edge  nodes.            */
struct slist_e {
    link_e head;
    link_e tail;
};

/*struct node_adj: Adajencies of a certain vertex.           */
/* e: Pointer to respective edge.                            */
/* dual: Pointer to back edge.                               */
/* next: Pointer to next adjacence of the certain vertex.    */
struct node_adj {
    edge e;
    edge dual;
    link_e next;
};

/*struct node_vert: Used by Queue, Heights.                 */
/* v: Pointer to respective edge.                           */
/* next: Pointer to nexte vertex on the linked list.        */
struct node_vert {
    vert v;
    link_v next;
};

/*struct cord: Used to find minimum cut.                    */
/* in: Id of souce of a connection in min cut.              */
/* out: Id of destiny of a connection in min cut.           */
typedef struct cord{
    int in;
    int out;
}*coord;

/* Queue */
list_v create_list_v();
void insertEndV(list_v, vert v);
void removeBeginV(list_v); 

/* Height */
link_v insertBeginV(link_v, vert);
link_v removeV(link_v, int);  
link_v h_removeBeginV(link_v);

/* Edges & Back Edges*/
list_e create_list_e();
void insertBeginE(list_e,edge);
void insertEndE(list_e, edge , edge);

/* Creates Edge and Vertex*/
edge create_edge(vert, int, int);
vert create_vertex(int, int);

/* Max flow functions */
void preflow(graph);
void addBackEdge(link_e, vert, int);
void dicharge(graph, vert);
void push(graph,link_e, vert);
void relabel(graph, vert);

/* Minimum Cut function */
void minimumCut(link_v);
int compareConnect(const void *a, const void *b);
int compareGas (const void * a, const void * b);

/* Free heights */
void free_vertex_edges_height(vert, link_v);

/* main                                                             */
/* furn: Number of supliers.                                        */
/* comb: Number of Gas stations.                                    */  
/* connect: Number of connections.                                  */
/* startF: When id of supliers start.                               */
/* startComb1: When id of gas stations start.                       */
/* startComb2: When id of the other vertex of gas station start.    */
/* capacity: Capacity of each connection and gas station.           */
/* in: Source vertex of an edge.                                    */
/* out: Destiny vertex of an edge.                                  */
int main(){
    int furn = 0, comb = 0, connect = 0;
    int startF = 0, startComb1 = 0, startComb2 = 0;
    int i = 0, p = 0, capacity = 0;
    int in = 0,out = 0;
    vert v = NULL;
    edge e = NULL;

    graph grid = (graph) calloc(1,sizeof(struct grid));

    if(scanf("%d %d %d",&furn, &comb, &connect) != 3)
          perror("Invalid Input");

    startF = 2;
    startComb1 = furn+2;
    startComb2 = startComb1 + comb;
    grid->nVertices = 2 + 2*comb + furn;
    
    grid->vertex = (vert *) calloc(grid->nVertices, sizeof(vert));         /* Memory reservation for all vertex's.*/
    grid->heights = (link_v *) calloc(grid->nVertices+1,sizeof(link_v));   /* Memory reservation for all verheights.*/
    
    grid->queue = create_list_v();                                         /* Initialize Queue linked list.*/

    grid->vertex[1] = create_vertex(1,grid->nVertices);                    /* Initialize vertex source (hiper) with height = nVertices*/
    grid->vertex[1]->adj = create_list_e();                                /* Initialize Vertex source adjacencies.*/

    grid->heights[grid->nVertices] = insertBeginV(grid->heights[grid->nVertices], grid->vertex[1]);  /* Insert source (hiper) in the respective height. */

    grid->vertex[0] = create_vertex(0,0);                                  /* Initialize vertex target (fictional target that is conected to all the supliers). */
    grid->vertex[0]->adj = create_list_e();                                /* Initialize vertex target adjacencies. */

    grid->heights[0] = insertBeginV(grid->heights[0], grid->vertex[0]);    /* Insert target in the respective height. */


    for(i=startF; i<startComb1; i++) {
        if(scanf("%d",&capacity) != 1)
            perror("capacity");
        grid->vertex[i] = create_vertex(i,0);         /* Initialize vertex with height 0. */
        grid->heights[0] = insertBeginV(grid->heights[0], grid->vertex[i]);     /* Insert vertex in the respective height. */
        grid->vertex[i]->adj = create_list_e();       /* Initialize vertex adjacencies. */

        e = create_edge(grid->vertex[0], capacity, 0);   /* Initialize adjacency with the respective capacity and flow 0. */
        insertBeginE(grid->vertex[i]->adj, e);      /* Insert the edge between the gas station. */
             
    }

    for(i = startComb1,p = startComb2; i < startComb2 ;i++,p++) {
        if(scanf("%d",&capacity) != 1)
            perror("capacity");
        /* A gas station has a certain capacity so its divided in two vertex with respective capacity between them.  */
        grid->vertex[i] = create_vertex(i,0);       /* Initialize vertex with height 0. */
        grid->vertex[p] = create_vertex(-i,0);      /* Initialize vertex with -id to know that is the same gas station.*/
        grid->vertex[i]->adj = create_list_e();     /* Initialize vertex adjacencies. */
        grid->vertex[p]->adj = create_list_e();     /*Initialize vertex adjacencies. */
        grid->heights[0] = insertBeginV(grid->heights[0], grid->vertex[i]);     /* Insert vertex in the respective height. */
        grid->heights[0] = insertBeginV(grid->heights[0], grid->vertex[p]);     /* Insert vertex in the respective height. */

        e = create_edge(grid->vertex[p], capacity, 0);   /* Initialize adjacency with the respective capacity and flow 0. */
        insertBeginE(grid->vertex[i]->adj, e);           /* Insert the edge between the gas station. */
    }

    for(i=0; i<connect; i++) {
    
        if(scanf("%d %d %d",&out, &in, &capacity) !=3)
            perror("conection");

        if(in >= startComb1) {                          /* If the adjacency has source the gas station then it belongs to -id.*/ 
            e = create_edge(grid->vertex[out], capacity, 0);
            insertBeginE(grid->vertex[in + comb]->adj, e);
            continue;
        }

        e = create_edge(grid->vertex[out], capacity, 0);
        insertBeginE(grid->vertex[in]->adj, e);
    
    }
    
    preflow(grid);

    while(grid->queue->head != NULL) {   /* Queue keeps the vertex's with excess if Queue is empty then max flow terminates. */
        v = grid->queue->head->v;
        removeBeginV(grid->queue);       /* Removes the element from Queue because of dicharge (excess becomes 0). */
        dicharge(grid, v);               /* Dicharge of vertex deleted from queue. */
    }
    
    printf("%d\n",grid->vertex[0]->excess); /* Prints MaxFlow. */

    minimumCut(grid->heights[grid->nVertices]); /* Find Min Cut and prints it */

    /* Free of all the variables */
    for(i=0; i<grid->nVertices; i++) {
        free_vertex_edges_height(grid->vertex[i],grid->heights[i]); 
    }

    free_vertex_edges_height(NULL,grid->heights[grid->nVertices]);
    
    free(grid->vertex);
    free(grid->queue);
    free(grid->heights);
    free(grid);
    
    return 0;

}

/* preflow:                                                                                               */
/* Search all the edges of source and sends all the capacity of those vertices to the respetive destiny   */
/* Creates back edges with -flow form destiny vertex to source vertex with capacity 0 to be able to send  */
/* back if needed.                                                                                        */
 void preflow(graph grid) {
    link_e i = NULL;

    for(i = grid->vertex[1]->adj->head; i != NULL; i=i->next){
        i->e->out->excess = i->e->capacity;
        i->e->flow = i->e->capacity;
        addBackEdge(i, grid->vertex[1], i->e->capacity);
        insertEndV(grid->queue, i->e->out);
    }
}

/*addBackEdge: Creates the new edge (Back edge). And inserts the edge at the end of the linked list of the vertex (in) adjacencies */
void addBackEdge(link_e prev_edge, vert new_out, int flow){ /*new out is the in of the prev edge*/
    edge e = create_edge(new_out, 0, -flow);  
    prev_edge->dual = e;
    insertEndE(prev_edge->e->out->adj, e, prev_edge->e);
}   

/* discharge:                                                                          */
/* While vertex has excess then we search for every adjacency of that vertex.          */
/* Does push, if vertex adjacency + 1 is equal to the vertex and can still send flow.  */
/* If we search  for all adjacecncies and still has excess then we relabel the vertex. */
void dicharge(graph grid, vert v) {
    list_e t = v->adj;
    link_e edge = t->head;
    while(v->excess > 0) {
        if(edge == NULL) {
            relabel(grid, v);
            edge = v->adj->head;
        }
        if(v->height == (edge->e->out->height + 1) && edge->e->flow != edge->e->capacity ) { 
            
            push(grid,edge,v);
        }
        edge = edge->next;
       
    }
}
 
/* push:                                                                                      */
/* Pushes minimum of excess and edge capacity - edge flow.                                    */
/* If the destiny vertex had excess 0 then we insert him in the Queue.                        */
/* Increase destiny vertex excess and edge flow.                                              */       
/* If doesn't have a backedge than we create it.                                              */
void push(graph grid, link_e edge, vert v) {
    int flow_left = edge->e->capacity - edge->e->flow;
    int flow = MIN(v->excess,flow_left);
    v->excess -= flow;

    if( edge->e->out->excess == 0 && flow > 0 && (edge->e->out->id > 1 || edge->e->out->id < 0) ) {
        insertEndV(grid->queue, edge->e->out);
    }
    edge->e->flow +=  flow;
    edge->e->out->excess += flow;
    if(edge->dual == NULL) {
        addBackEdge(edge, v, flow);
    } else {
        edge->dual->flow -= flow;
    }
}

/* relabel:                                                                                                                   */
/* If height of the vertex is not bigger than the number of vertices: Remove the respective index i = height for height[i].   */
/* If height is bigger than the number of vertex then we already have the element in height[number of vertices]               */
/* and it belongs to on side of the minimum cut.                                                                              */
/* If the linked list of the respective height is equal to null after we remove the element, we aply gap heuristic.           */
/* Gap heuristic: Put the vertex in the linked list of the height[nVertices], and does the same to all the next heights until */
/* finding an empty linked list or heights = nvertices.                                                                       */
/* If not satisfies the conditions for gap heuristics than we just increase the height to the minimum of all                  */
/* heights adjacencies.                                                                                                       */
/* If this new height is less than nVertices we also insert the vertex in the linked list of this new height, else            */
/* if the old height was less than nVertices, that means she jumped to a greater height and it belongs to one side of the cut */
/* so we need to add her to the linked list of height equal to nVertices                                                      */  
void relabel(graph grid, vert v){
    int oldHeight = v->height;
    int i = 0;
    vert vertex = NULL;
    int nVertices = grid->nVertices;
    int min_height = grid->nVertices*2;
    link_e adj = v->adj->head;
    

    if(oldHeight < nVertices) {
         grid->heights[oldHeight] = removeV(grid->heights[oldHeight], v->id);
    }

    if(((grid->heights[oldHeight] == NULL) && (oldHeight < nVertices))) {
        
        grid->heights[nVertices] = insertBeginV(grid->heights[nVertices], v);
        v->height = nVertices + 1;

        for(i = oldHeight+1; (i < nVertices); i++) {
            
            while (grid->heights[i] != NULL) {
                vertex = grid->heights[i]->v;
                
                grid->heights[i] = h_removeBeginV(grid->heights[i]);
                grid->heights[nVertices] = insertBeginV(grid->heights[nVertices], vertex);
                vertex->height = nVertices + 1;
            }

        }

        return;
    
    }

    for(; adj != NULL; adj = adj->next) {
        if(adj->e->out->height < min_height && adj->e->flow != adj->e->capacity) 
            min_height = adj->e->out->height;
    }

    if(min_height < nVertices ) {
        grid->heights[min_height+1] = insertBeginV(grid->heights[min_height+1], v);
    } else if (oldHeight < nVertices) {
         grid->heights[nVertices] = insertBeginV(grid->heights[nVertices], v);
    }
    
    v->height = min_height+1;

}

/* minimumCut: In the linked list of the grid->height[nVertices] we have access one side of the minimum cut,                  */           
/* So we search all the vertex's in the linked list and check all the adjacencies, if height of vertex is is                  */
/* bigger than the out edge height + 1 then we found a conection of minimum cut.                                              */
/* If the conections (edges) have in = -out than we have a gasStation and insert it to the vector with all the gas Stations.  */
/* Else we have a conection and we insert it to the vector with a structer of vertice in and out.                             */
/* We invert the conections because we the inverse graph.                                                                     */
/* We aply quick sort to both vectors and print them.                                                                         */


void minimumCut(link_v gapVertex) {
    coord minCutConect = (coord) malloc(10*sizeof(struct cord));
    int * minCutGas = (int*) malloc(10*sizeof(int));
    int mallConections = 10, mallGas = 10, i = 0, nConections=0, nGas=0;
    link_v gap = gapVertex;
    link_e gapEdges = NULL;

    for(; gap!= NULL; gap = gap->next) {

        gapEdges = gap->v->adj->head;
        
        while(gapEdges != NULL) {
        
            if(gap->v->height > (gapEdges->e->out->height + 1) && gapEdges->e->capacity != 0 && gapEdges->e->out->id != 0) {
        
                if(nConections == mallConections-1) {
                    mallConections *= 2; 
                    minCutConect = (coord) realloc(minCutConect,sizeof(struct cord)*mallConections);
        
                } 
                if (nGas == mallGas-1) {
                    mallGas *= 2; 
                    minCutGas = (int*) realloc(minCutGas,sizeof(int)*mallGas);
                }
                
                if(-gapEdges->e->out->id == gap->v->id ) {
                    minCutGas[nGas] = gap->v->id;
                    nGas++;
                } 
                
                else {
                    minCutConect[nConections].in = abs(gapEdges->e->out->id);
                    minCutConect[nConections].out = abs(gap->v->id);
                    nConections++;
                }
            }
            
            gapEdges = gapEdges->next;         
        }        
    }
    qsort(minCutGas,nGas,sizeof(int), compareGas);
    qsort(minCutConect, nConections, sizeof(struct cord),compareConnect);

    if(nGas == 0)
        printf("\n");

    while(i<nGas-1) {
        printf("%d ", minCutGas[i]);
        i++;
    }
    if(nGas != 0)
        printf("%d\n",minCutGas[i]);

    for(i=0; (i<nConections); i++)
        printf("%d %d\n", minCutConect[i].in, minCutConect[i].out);
    
    free(minCutConect);
    free(minCutGas);
}

/* Creates Edge and Vertex */

edge create_edge(vert v, int capacity, int flow) {
    edge e = (edge)calloc(1,sizeof(struct e));
    e->capacity = capacity;
    e->out = v;
    e->flow = flow;
    return e;
}

vert create_vertex(int id, int height) {
    vert v = (vert)calloc(1,sizeof(struct v));
    v->excess = 0;
    v->height = height;
    v->id = id;
    v->adj = NULL;
    return v;
}

/* QUEUE */

/* Creates doubly linked list */
list_v create_list_v() {
    list_v i = (list_v) calloc(1, sizeof(struct slist_v));
    i->head = i->tail = NULL;
    return i;
}


/* Inserts new vertice at the end of the doubly linked list */
void insertEndV(list_v lis, vert v){
    link_v newEl = (link_v)calloc(1,sizeof(struct node_vert));
    newEl->v = v;
        newEl->next = NULL;

    if(lis->head == NULL){
        lis->head = lis->tail = newEl;
        return;
    }
    
    lis->tail->next = newEl;
    lis->tail = lis->tail->next;  
}

/* Removes the first vertex if not empty */
void removeBeginV(list_v lis) {
    link_v newEl=NULL;

    if(lis->head == NULL ) {
        return;
    }

    if(lis->head->v->id == lis->tail->v->id){
        newEl = lis->head;
        free(newEl);
        lis->head = NULL;
        return;
    }
    
    newEl = lis->head;
    lis->head = lis->head->next;
    free(newEl);
}

/* Height */

/* Inserts vertex at the begining of the linked list */
link_v insertBeginV(link_v head, vert v) {
	link_v newEl = calloc(1,sizeof(struct node_vert));

    newEl->v = v;
	newEl->next = head;

    head = newEl;
    return head;
}

/* Removes the first element of the linked list */
link_v h_removeBeginV(link_v head) {
    link_v newEl=NULL;
    
    if(head == NULL ) {
        return head;
    }


    newEl = head->next;
    free(head);

    return newEl;
}

/* Removes an element of the linked list */
link_v removeV(link_v head, int id) {
    link_v ele = head;
    link_v prev_ele = NULL;
     
    if(head != NULL && head->v->id == id) {
       
        ele = head->next;
        free(head);
        return ele;
    }
    
    while(ele != NULL && ele->v->id != id) {
        prev_ele = ele;
        ele = ele->next;
    }
    if(ele == NULL || prev_ele == NULL) return head;

    prev_ele->next = ele->next;
    free(ele);

    return head;

}

/* Edges */

/* Creates a doubly linked list for edges */
list_e create_list_e() {
    list_e i = (list_e) calloc(1,sizeof(struct slist_e));
    i->head = i->tail = NULL;
    return i;
}

/* Inserts at the begining of the doubly linked list*/
void insertBeginE(list_e lis, edge e) {
	link_e newEl = (link_e)calloc(1,sizeof(struct node_adj));
	newEl->e = e;
    
    if(lis->head == NULL){
        lis->head = lis->tail = newEl;
        return;
    }

    newEl->next = lis->head;
    lis->head = newEl;

}

/* Inserts new vertice at the end of the  doubly linked list */
void insertEndE(list_e lis, edge e, edge dual){
	link_e newEl = (link_e)calloc(1,sizeof(struct node_adj));
    newEl->e = e;
    newEl->next = NULL;
    newEl->dual = dual;

    if(lis->head == NULL){
        lis->head = lis->tail = newEl;
        return;
    }
    lis->tail->next = newEl;
    lis->tail = lis->tail->next;   
}

/* Free of all vertex's, heights, and adjancies */
void free_vertex_edges_height(vert vertex, link_v height) {
    link_e aux = NULL, aux2 = NULL;
    link_v height_aux = height, height_aux2 = NULL;
    
    if(vertex !=NULL)
        aux = vertex->adj->head;

    while(aux != NULL || height_aux != NULL) {
    
        if(aux != NULL) {
             aux2 = aux;
            free(aux->e);
            aux = aux->next;
            free(aux2);
        }
    
        if(height_aux != NULL ) {
            height_aux2  = height_aux;
            height_aux = height_aux->next;
            free(height_aux2);
        }
    }

    if(vertex!=NULL) {
        free(vertex->adj);
        free(vertex);
    }
}

/* Compares if first in is bigger then the second in, if it is equal than if fist out is equal to second out*/
int compareConnect(const void *a, const void *b) {

  coord first = (coord) a;
  coord second = (coord) b;

  int compare =  first->in - second->in;

  if (compare < 0)
    return -1;

  if (compare > 0)
    return 1;

  return first->out - second->out;
}

/* Compares if first id is bigger then second id*/
int compareGas (const void * a, const void * b) {
   return ( *(int*)a - *(int*)b );
}
