import random
f = open('INPUT.txt','wt')
random.seed(1023)
for _ in range(0,20000):
    f.write(str(random.randint(0,1)))
f.close()
