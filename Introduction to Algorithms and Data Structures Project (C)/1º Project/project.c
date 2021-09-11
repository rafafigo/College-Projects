/*****************************************
 *      Rafael Figueiredo 90770          *
 *****************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define comp_ele 20000
#define ele 10000
#define name 81

int select_letter();
void add_value();
void list_ele();
void list_limits_density();
void list_line_matrix();
void list_column_matrix();
void ord_line_column_matrix();
void define_zero_matrix();
void compress_matrix();
void save_ele();
void insertion_sort(int n, unsigned long l, unsigned long r);
void erase_value();
void new_max_min();
void erase_line_column(unsigned long l, unsigned long c);


typedef struct{
	unsigned long line;
	unsigned long column;
	double val;

} value;


FILE *fp;
char filename[name] = "default.txt"; 	/* Default name if you save the matrix in a file*/
unsigned long mini,minj,maxi,maxj, num_elements=0;
int matrix_input=0; 
value vector_matrix[ele]; 	/* Vector with all the elements that are different from "zero" */
double zero = 0;


int main(int argc, char *argv[]){

	if (argc == 2) {
		strcpy(filename,argv[1]);	/* If we save at any point the matrix (line, column and value) without a new file name the matrix is saved on this file  */
		fp = fopen(argv[1],"r");	/* In case that it's given a matrix input, open the file and read the values */
		matrix_input = 1;

		while (matrix_input == 1)	/* Matrix input is a variable that tell us if we reach the end of file */
			add_value();
		
		fclose(fp);
	}

	while (select_letter()); 	/* While the letter "q" isn't inserted, we search for a letter */

	return 0;  

}

/* Select the command given */ 
int select_letter(){
	char comand;

	scanf(" %c",&comand);
	switch(comand) {
		case 'a': add_value(); return 1; 

		case 'p': list_ele(); return 1;

		case 'i': list_limits_density(); return 1;

		case 'l': list_line_matrix(); return 1;

		case 'c': list_column_matrix(); return 1;

		case 'o': ord_line_column_matrix(); return 1;

		case 'z': define_zero_matrix();return 1;

		case 's':
			compress_matrix(); return 1;

		case 'w':
			save_ele(); return 1;

		case 'q': return 0;	/* End of program */

		default:
			return 1;
	}


}

/* This function verifies if  it's given valid values for the column, line and values;
Verifies if the value is zero and if it is and belongs to the vector we eliminate this line and column in the vector;
If the value is already in the vector we replace it whith the new value;
And if it passes all conditions is added as a new element of the vector; */
void add_value(){
	unsigned long poss_line, poss_column, count;
	int already=0;
	double val;

	if (matrix_input == 1){
		if (fscanf(fp,"%lu %lu %lf",&poss_line,&poss_column,&val)!=3){ 	/* To keep adding values we need to get 3 inputs, else we verify that the matrix input ended */
			matrix_input=0;
			return;
		}
	}

	else if (scanf("%lu %lu %lf",&poss_line,&poss_column,&val)!=3)	/* To add the value it needs 3 inputs, else search for another letter */
		return;

	if (val == zero){
		already = 1;
		erase_line_column(poss_line, poss_column);	/* If value we want to add is equal to "zero" we need to delete the line and the column from the vector by going to this function */
	}

	if (val != 0){
		for(count=0;count!=num_elements;count++) 
			if (vector_matrix[count].line == poss_line && vector_matrix[count].column == poss_column){	/* line and Column are in the vector so we replace the old value for the new value */
				already = 1;
				vector_matrix[count].val = val; 
				break;
			}
	}

	if (already == 0 && val != zero){ 	/* Adds the new element to the vector_matrix */
		vector_matrix[num_elements].line = poss_line;
		vector_matrix[num_elements].column = poss_column;
		vector_matrix[num_elements].val = val;
		num_elements++;
                                     
		if (num_elements == 1){ 	/* If the vector has only one element, max/min dimensions = dimensions of vector_matrix*/
		mini=vector_matrix[0].line;
		minj=vector_matrix[0].column;
		maxi = vector_matrix[0].line;
		maxj = vector_matrix[0].column;
		}
		
		else{						  /* Change the min/max of line/column if the dimensions are changed */
			if (mini>poss_line)  
				mini = poss_line;

			if (minj>poss_column)
				minj = poss_column;

			if (maxi < poss_line)
				maxi = poss_line;

			if (maxj < poss_column)
				maxj = poss_column;
		}
	}

}

/* Erases the line and the column because they have a "zero" value on the vector_matrix */
void erase_line_column(unsigned long l,unsigned long c){
	unsigned int count,i;

	for(count=i=0; count<num_elements;count++){
		if (vector_matrix[count].line != l || vector_matrix[count].column!=c)
			vector_matrix[i++] = vector_matrix[count]; 	/*Creates the vector_matrix from the beginning but this time without the line and column with the value "zero" */ 
	}		

	num_elements = i; 
	new_max_min(); 	/* Rewrite dimensions of the matrix */

}

/* Shows all elements of the matrix that are diffrent from "zero" by searching in the vector_matrix the elements */
void list_ele(){
	unsigned int i;

	if (num_elements==0) 	/* If the matrix is all "zero" */
		printf("empty matrix\n");

	else
		for (i=0;i<num_elements;i++) 	/* Prints all the elements on the vector diferent from "zero"  */
			printf("[%lu;%lu]=%.3f\n",vector_matrix[i].line,vector_matrix[i].column,vector_matrix[i].val); 

}

void list_limits_density(){
	float dens;
	int size;

	if (num_elements==0)    /* If the matrix is all "zero" */
		printf("empty matrix\n");

	else{                  /* Prints dimensions of the matrix of lines and columns, number of elements diferent from "zero" size and density*/ 
		size = (maxi-mini+1)*(maxj-minj+1); 
		dens = (num_elements*100.0)/size;
		printf("[%lu %lu] [%lu %lu] %lu / %d = %.3f%%\n",mini,minj,maxi,maxj,num_elements,size,dens); 
	}

}

/* Prints all elements from a line inside the maximum and minumum of the matrix */
void list_line_matrix(){
	unsigned long line_list, i, p;
	int null=0;

	if (scanf("%lu",&line_list)==1){
		if (line_list>maxi || line_list<mini)		/* If line is outside the limits of dimension with non "zeros"*/
			printf("empty line\n");

		else{
			for (i=0;i<num_elements;i++) 	/* Is the line empty? */
				if (line_list == vector_matrix[i].line){ 
					i = num_elements + 5;
					break;
				}

			if (i!=num_elements + 5)	 /*line with only "zeros" */
				printf("empty line\n");

			else{	 /* Prints all elements of the line inside the dimensions of the matrix */
				for(i=minj;i<=maxj;i++){
					for(p=0;p<num_elements;p++){
						if (vector_matrix[p].line == line_list && vector_matrix[p].column == i ){
							null = 0;
							printf(" %.3f",vector_matrix[p].val); 
							break;
						}

						else
							null = 1;
					}

					if (null == 1)
						printf(" %.3f",zero); /* The column in this line has the value"zero" */
				}

				printf("\n");
			}
		}

	}

}

/* Prints all elements from a column inside the dimensions of the matrix */
void list_column_matrix(){
	unsigned long colu_list, i, p;
	int null=0;
	if (scanf("%lu",&colu_list)==1){
		if (colu_list>maxj || colu_list<minj) 	/* Column with only "zeros" outside off dimensions */
			printf("empty column\n"); 

		else{
			for (i=0;i<num_elements;i++)
				if (colu_list == vector_matrix[i].column){ 	/* Is the column all "zero"? */
					i = num_elements + 5;
					break;
				}

			if (i!= num_elements + 5) 	/* line with only "zeros" if it's true*/
				printf("empty column\n");

			else {
				for(i=mini;i<=maxi;i++){ 
					for(p=0;p<num_elements;p++){ 	/* Search for the column with index (i) in vector_matrix until: End of the elements or found the column  */
						if (vector_matrix[p].column == colu_list && vector_matrix[p].line == i){
							null = 0; 	/* Found the column so: Turns off the variable that places de column with the value of "zero" */
							printf("[%lu;%lu]=%.3f\n",vector_matrix[p].line,vector_matrix[p].column,vector_matrix[p].val); /* Prints the line, column and value non "zero"*/
							break; 
						}
						else 
							null = 1; 
				}
				if (null == 1)
					printf("[%lu;%lu]=%.3f\n",i,colu_list,zero); 	/* Prints with the value "zero" */
				}
			}
		}

	}

}

/* In this funcion we order by line and inside the line by columns if we only write 'o' or by columns and inside the column by lines */
void ord_line_column_matrix(){
	char space,col[7], col_t[7]="column";
	int first_col; 	/* Indicates if in insertion we order by columns or by lines */
	unsigned long nl = 0;

	scanf("%c", &space); 	/* gets the '/n', and that means that we don't write "column" or ' , this means that we need to see if it's followed by column  */
	if (space =='\n'){ 
		first_col = 1;   	/* We use insertion for order the columns and then again the insertion for the lines */
		insertion_sort(first_col, nl, num_elements-1);
		first_col = 0;
		insertion_sort(first_col, nl, num_elements-1);	
	}

	else{
		scanf("%s",col); 
		if (strcmp(col,col_t)==0){ 	/* Check if the word is 'column' */
			first_col = 0; 	/* We use insertion for order the lines and then again the insertion for the column */
			insertion_sort(first_col, nl, num_elements-1);
			first_col = 1;
			insertion_sort(first_col, nl, num_elements-1);
		}
	}

}
/* Algorithm Insertion Sort - Used two times by order */
void insertion_sort(int n, unsigned long l, unsigned long r){
	unsigned long i, p;
	value vect_mat;

	for (i=l+1; i<= r; i++){
		vect_mat = vector_matrix[i]; 	/* Keeps the variable with the element  that is moving */
		p = i -1;

		if (n==0)  	/*Order the line*/
			while (p>=l && vect_mat.line<vector_matrix[p].line){
				vector_matrix[p+1] = vector_matrix[p]; 	/* Push values to the right */
				p--;
			}

		else if (n==1) 	/*Order the Column*/
			while (p>=l && vect_mat.column < vector_matrix[p].column){
				vector_matrix[p+1] = vector_matrix[p]; 	/* Push elements to the right */
				p--;
			}

		vector_matrix[p+1] = vect_mat; 	/* Put the element where is suposed to be */ 
	}
}

/* Defines a new value for "zero" */
void define_zero_matrix(){
	double new_zero;

	if (scanf("%lf",&new_zero)==1){
		if (zero != new_zero){
			zero = new_zero; 	/* Changes the value of "zero" */
			erase_value();		/* Goes to a function that deletes all elements with value "zero" from the elements of the matrix_input */ 
		}
	}

}

/* If there's a element whith the value of "zero" in vector_matrix, erase that element */
void erase_value(){
	unsigned long count,i;

	for(count=i=0; count<num_elements;count++){ 	/* Creates the vector with the elements from the beguining but without the new "zero" */
		if (vector_matrix[count].val != zero)
			vector_matrix[i++] = vector_matrix[count];
	}		
	num_elements = i; 	/* New number of elements */
	new_max_min();		/* Rewrite dimensions of matrix  */

}

/* Rewrite Dimensions */
void new_max_min(){
	unsigned long x;
	if (num_elements>0){ 	/*Check if there's at least one element to check, if yes, inicitialize min,max of lines and columns with the dimensions of first element */
		mini=vector_matrix[0].line;
		minj=vector_matrix[0].column;
		maxi = vector_matrix[0].line;
		maxj = vector_matrix[0].column;

		for(x=1;x<num_elements;x++){ 	/*Check all elements on the vector_matrix and see if there's one bigger for maxi and maxj and smaller for mini and minj */
			if (mini>vector_matrix[x].line)
				mini=vector_matrix[x].line;

			if (minj>vector_matrix[x].column)
				minj=vector_matrix[x].column;

			if (maxi<vector_matrix[x].line)
				maxi=vector_matrix[x].line;

			if (maxj<vector_matrix[x].column)
				maxj=vector_matrix[x].column;
		}
		
	}

}

/* Save the line, column and value of the elements in a file with the name that is given or the one that is already exists by default */
void save_ele(){
	unsigned long i;
	char space;
	FILE *fs;

	scanf("%c",&space);
	if (space==' ') 		/*If not uses: the name that was given before (by default or by another instruction) */
		scanf( " %s", filename);	/* Means that you entered the file name where you want the file saved */

	fs = fopen(filename, "w");
	
	for(i=0;i<num_elements;i++)
		fprintf(fs,"%lu %lu %.3f\n",vector_matrix[i].line,vector_matrix[i].column,vector_matrix[i].val); 	/* Print the elements for the file */
	
	fclose(fs);

}

/*  Compress matrix by double-offset indexing, with 3 vectors (vector_val,vector_ind,vector_offset) */ 
void compress_matrix(){
	float dens_m; 
	unsigned long i,p,offset,max_comp=(maxj-minj+1),dens=0,n_ele_l;
	int put_val=0,big,vector_line[comp_ele], vector_offset[comp_ele], vector_ind[comp_ele];
	value vect_aux[ele];
	double vector_val[comp_ele];
	
	dens_m = (num_elements*100.0)/((maxi-mini+1)*(maxj-minj+1));

	if ( dens_m > 50.0)
		printf("dense matrix\n"); 

	else{


		for (i = 0;i<comp_ele;i++){  /*Initialize with 0 or "zero" the vector*/
			vector_val[i] = zero;
			vector_offset[i] = -5;
			vector_ind[i] = 0;
			vector_line[i] = 0;
			if (i<ele){      /* Initialize auxiliar vector with 0, the auxiliar vector contains only elements of the most densed line*/
				vect_aux[i].column = 0;
				vect_aux[i].line = 0;
				vect_aux[i].val = 0;
			}
		}

		for (i=0;i<num_elements;i++) /* Add's on the vector_line the number of times that the element is equal to the line */
			vector_line[vector_matrix[i].line]++;

		for(p=mini;p<=maxi;p++){ /* Goes through all the lines that are elements of the matrix */
			for (big=-1,i=mini;i<=maxi;i++)
				if (vector_line[i]>big){ /* See witch line has more density */
					big = vector_line[i];
					dens=i;
				}

			for (n_ele_l=0,i=0;i<num_elements;i++){ /* Creates a vector with the elements of the line with more density */
				if (vector_matrix[i].line == dens){ 
					vect_aux[n_ele_l] = vector_matrix[i];
					n_ele_l++;
					
				}
			}

			for(offset=0;offset<max_comp;offset++){    /* See if you can insert all the elements of the line in the index of column plus the offset on vector_val in the dimensions of the line*/
				for(i=0,put_val=0;i<n_ele_l;i++){
					if (vector_val[(vect_aux[i].column + offset-minj)]!=zero){
						put_val=1; /*If you find on that index a value diffrent from "zero", breaks the for and tries again with next offset */
						break;
					}
				}

				if (put_val==0)  /*All positions on vector_val are "zeros" so end of second for. */
					break;
			}

			vector_offset[dens] = offset; /* Adds offset to the index of the line */
			vector_line[dens] = -5; /*Density of line -> dens, is turned negative this way the line is not repeated*/

			for(i=0;i<n_ele_l;i++){
				vector_val[(vect_aux[i].column + offset-minj)] = vect_aux[i].val;	/* Adds value in the position: number of column plus offset,where the first column in the dimensions of the vector_matrix is consider the minimum column */

			 	vector_ind[(vect_aux[i].column + offset-minj)] = vect_aux[i].line;	/* Adds line in the position: number of column plus offset,where the first column in the dimensions of the vector_matrix is consider the minimum column */
			}

			if (max_comp < maxj-minj+1 + offset) /* Verifies if the boundaries of the columns with the new offset are larger than previosly */
					max_comp = offset + maxj-minj+1; /* Gets the max of column with the compress matrix done*/	 
		}

	printf("value =");
	for(i=0;i< max_comp;i++)  /* Get's the values from the begining until the end max_comp */
		printf(" %.3f",vector_val[i]);

	printf("\nindex =");

	for(i=0;i< max_comp;i++)  /*Get the index of the line */
		printf(" %d",vector_ind[i]);

	printf("\noffset =");

	for(i=mini;i<=maxi;i++)            /*All the offsets used */
		if (vector_offset[i] != -5)
			printf(" %d",vector_offset[i]);
	printf("\n");

	}

}



