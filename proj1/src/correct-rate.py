import numpy as np
input_F = open('INPUT.txt')
output_F = open('testoutput.txt')
input_all = input_F.readlines()
output_all = output_F.readlines()
length = len(input_all[0])
input_data = input_all[0]
output_data = output_all[0]
correct_number = 0
error = 0
for i in range(0,length):
    try:
        if (output_data[i] == input_data[i]):
            correct_number+=1
        else:
            print("--> error! at: " +str(i)+ " correct should be: "+ str(input_data[i]))
    except:
        print("length not match error!")
        error=1
        break
if(error == 0):

    print("correct_number: "+str(correct_number))
    print("total length: "+str(length))
    correct_rate = float(correct_number)/float(length)
    print("total correct rate: "+str(correct_rate))
