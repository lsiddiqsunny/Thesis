import os
import csv
file_arr = os.listdir('.\\TEST_SET\\')
with open('linenumber.csv', 'w', newline='') as file:
    writer = csv.writer(file)
    for file in file_arr:
        if '4199' in file or '4218' in file or '4267' in file or '4278' in file or '4300' in file:
            continue
        # with open('D:\\Thesis\\Getafix\\gumtree-spoon-ast-diff+clustering\\Before\\before_'+file[7:-5]+'.java', 'r+') as f:
        #     content = f.read()
            # f.seek(0, 0)
            # f.write('public '+content)
        linenum = ''
        lineset = set()
        # with open('D:\\Thesis\\Getafix\\gumtree-spoon-ast-diff+clustering\\Before\\before_'+file[7:-5]+'.java','r') as myFile:
        #     print(' IN '+file[7:-5]+" : ")
        #     lookup = 'select '
        #     for num, line in enumerate(myFile, 1):
        #         if lookup.lower() in line.lower():
        #             linenum+=str(num)+' '
        #             lineset.add(num)
        #             print ('found at line:', num)

        # with open('D:\\Thesis\\Getafix\\gumtree-spoon-ast-diff+clustering\\Before\\before_'+file[7:-5]+'.java','r') as myFile:
        #     lookup = 'insert '
        #     for num, line in enumerate(myFile, 1):
        #         if lookup.lower() in line.lower():
        #             linenum+=str(num)+' '
        #             lineset.add(num)
        #             print ('found at line:', num)
        # with open('D:\\Thesis\\Getafix\\gumtree-spoon-ast-diff+clustering\\Before\\before_'+file[7:-5]+'.java','r') as myFile:                   
        #     lookup = 'delete '
        #     for num, line in enumerate(myFile, 1):
        #         if lookup.lower() in line.lower():
        #             linenum+=str(num)+' '
        #             lineset.add(num)
        #             print ('found at line:', num)
        # with open('D:\\Thesis\\Getafix\\gumtree-spoon-ast-diff+clustering\\Before\\before_'+file[7:-5]+'.java','r') as myFile:

        #     lookup = 'update '
        #     for num, line in enumerate(myFile, 1):
        #         if lookup.lower() in line.lower():
        #             linenum+=str(num)+' '
        #             lineset.add(num)
        #             print ('found at line:', num)

        # with open('D:\\Thesis\\Getafix\\gumtree-spoon-ast-diff+clustering\\Before\\before_'+file[7:-5]+'.java','r') as myFile:

        #     lookup = 'createStatement'
        #     for num, line in enumerate(myFile, 1):
        #         if lookup.lower() in line.lower():
        #             linenum+=str(num)+' '
        #             lineset.add(num)
        #             print ('found at line:', num)
        with open('.\\TEST_SET\\'+file,'r', encoding='utf8', errors='ignore') as myFile:

            lookup = 'execute'
            for num, line in enumerate(myFile, 1):
                if lookup.lower() in line.lower():
                    linenum+=str(num)+' '
                    lineset.add(num)
                    print ('found at line:', num)
        linenum = ""
        for x in lineset:
            linenum+=str(x)+' '
        os.system('java Fixer '+'D:\\Thesis\\Undergraduate-Thesis\\Code_base_for_PSR_ALGO\\TEST_SET\\'+file+' '+linenum) 
        writer.writerow([file[7:-5] , linenum])


        
