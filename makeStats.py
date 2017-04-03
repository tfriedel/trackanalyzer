__author__ = 'Thomas'

""" calculate mirex score between keys a and b (camelot notation)

MIREX score weightings
Key relation Score
Exact match (tonic) 1.0
Perfect Fifth (dominant) 0.5
Perfect fourth (subdominant) 0.5
Relative major/minor 0.3
Parallel major/minor 0.2
"""

import sys
def mirexScore(a, b):
    a_nr = int(a[:-1])
    b_nr = int(b[:-1])
    a_gender = a[-1]
    b_gender = b[-1]
    if a==b:
        return 1.0
    elif a_nr == b_nr:
        return 0.3
    elif ((a_nr-b_nr)%12==1 or (b_nr-a_nr)%12==1) and a_gender == b_gender:
        return 0.5
    elif ((a_gender == "A" and b_gender == "B" and ((a_nr+3) % 12) == (b_nr%12)) or \
         (a_gender == "B" and b_gender == "A" and (a_nr % 12) == ((b_nr + 3)%12))):
        return 0.2
    else:
        return 0.0

#f = file("c:\\users\\thomas\\key benchmark\\output.txt")
#f = file("C:\\Users\\Thomas\\key benchmark\\ibrahim modern\\1127010670 - ibrahim modern\\output.txt")
f = file(sys.argv[1])
lines = f.readlines()
lines = [line.split(";") for line in lines]
correct = [line[0].split("-",2) for line in lines]
oldlines = lines
lines = [line[1:] for line in lines]

sum = 0.0
correct_sum = 0
compatible_sum = 0
for i in range(len(lines)):
    score = mirexScore(lines[i][0],correct[i][0])
    if (score==1.0):
        correct_sum +=1
    elif (score>0):
        compatible_sum += 1
    sum += score
    print correct[i][0],lines[i][0],score,oldlines[i][0].split("-",2)[2]
print "mirex score: ",sum
print "mirex score normalized: ", sum/len(lines)
print len(lines)
print "correct: ", correct_sum, "compatible: ", compatible_sum, "incorrect: ", len(lines)-(correct_sum+compatible_sum)
