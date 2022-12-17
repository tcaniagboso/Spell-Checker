from english_words import english_words_set as engdict

#class that holds the list of correct words in a txt file
#and contains method to check if each word is in the English dictionary

class Words:

    def __init__(self,fileName):
        self.oldFile=fileName #file name as input by user
        self.correctWords=[] #list of correct words
        self.fileString='' #file string to reprint updated file
        newFileName=fileName[:-4] #new file name (removed .txt to add updated specification)
        self.newFile=f'{newFileName}_updated.txt' 

    def __str__(self):
        return self.fileString #returns list to print

    def checkSuffix(self, word): 
        ending_list = ['ing', 'd', 'ment','ness', 'ity', 'able', 'ion', 'ly', 'ful', 'ed', 'less', 's','r','.','?',',',':',';','!'] #possible suffix strings
        for ending in ending_list: #traverse through possible suffix 
            if ending in word: #if the suffix is in the word
                endLen=len(ending) #find length of suffix
                if word[-endLen:]==ending:
                    baseWord=word[0:-endLen] #find base word
                    if baseWord in engdict: #if base word is valid, return true
                        return True
        return False


    def checkWords(self):
        with open(self.oldFile,'r') as file: #opens file to read txt
            lines=file.readlines() #separates lines into list of strings
            indivWords=[]
            for line in lines: #traverses list of strings
                indivWords.extend(line.split()) #splits strings into individual words
                indivWords.extend('\n')
            for word in indivWords: #traverses list of individual words
                if word.lower() in engdict: #checks if word is valid
                    self.correctWords.append(word) #adds word to list if valid
                elif self.checkSuffix(word): #checks if there is a suffix and base word is valid
                    self.correctWords.append(word) #adds word to list if base word is valid
                else:
                    suggestion=SuggestedWords(word) #creates suggestion object for misspelled word
                    returned=suggestion.traverseToCompare() #returns accepted/original value based on user input
                    self.correctWords.append(returned) #adds returned value to correct words list
            #creating a string to use in updated .txt file
            for word in self.correctWords: #traverses through corrected words list
                #make sure that there is no space added when a new line is created
                if word != '\n': 
                    self.fileString+=word+' '
                else:
                    self.fileString+=word 
        #create a new .txt file with string of corrected words
        with open(self.newFile,'w') as file2:
            file2.write(self.fileString)
            
                

class SuggestedWords:

    def __init__(self,wrongWord):
        self._wrong=wrongWord #sets wrong word to lowercase
        if self.wrong[0]==self.wrong[0].upper():
            self._capitalized=True
        else:
            self._capitalized=False

    @property
    def wrong(self):
        return self._wrong 

    @property
    def capitalized(self):
        return self._capitalized
    
    def compareSpelling (self,correct):
        consonants = "bcdfghjklmnpqrstvwxyz" #establish consonants
        consonant_correct_count = 0 #count correct consonants in incorrect word
        consonant_wrong_count = 0 #count incorrect consonants in incorrect word
        correct_set = sorted(set(correct)) #make suggested word a set of chars
        wrong_set = sorted(set(self.wrong.lower())) #make original word a set of chars
        for x in correct_set: #traverse through set of suggested word chars
            #count the number of consonants
            if x.lower() in consonants:
                consonant_correct_count += 1
        for y in wrong_set: #traverse through set of original word chars
            #count the number of consonants
            if y.lower() in consonants:
                consonant_wrong_count += 1
        if consonant_correct_count == consonant_wrong_count and abs(len(correct)-len(self.wrong.lower()))<=1: #find similar number consonants and length
            if correct_set == wrong_set: #suggest if the same characters are in both words
                if self.capitalized: #if the word is capitalized, suggestion is capitalized
                    for index in range(len(correct)):
                        if index==0:
                            correct=correct.replace(correct[index],correct[index].upper(),1)
                print(f"Instead of: '{self.wrong}', did you mean '{correct}'?")
                return True
            elif abs(len(correct_set) - len(wrong_set)) == 1: #if there is an extra char
                common_set = sorted(set.intersection(set(correct), set(self.wrong))) #find the common chars
                if common_set==correct_set: #if the common chars are the same as the suggested word
                    if self.capitalized: #if the word is capitalized, suggestion is capitalized
                        for index in range(len(correct)):
                            if index==0:
                                correct=correct.replace(correct[index],correct[index].upper(),1)
                    print(f"Instead of: '{self.wrong}', did you mean '{correct}'?") #suggest
                    return True
        
    
    def traverseToCompare(self): #traverse through words in dictionary to compare to wrong word
        for word in sorted(engdict): #traverse dictionary
            broken=0 #keep track to break loops
            if self.compareSpelling(word.lower()): #if similar spelled suggestion returned
                checkAns=input('Type Y or N to respond: ') #prompt user whether suggested word was intended
                if checkAns.upper() =='Y': #if suggested word is correct
                    broken+=1 #increase tracker
            if broken>=1: #break loop and return correct word
                if self.capitalized: #if the original word is capitalized, the replaced word will be capitalized
                    for index in range(len(word)):
                        if index==0:
                            word=word.replace(word[index],word[index].upper(),1)
                return word
        return self.wrong #no suggestions were right: return original word

    


#expalin function of the script
print()
print('This program is a spell-checking application that goes through')
print('a text file, flags misspelled words, and suggests correct alternatives.')
print()

#explain how to use provided file and test
print('We included one file called \'test.txt\' that will run automatically.')
print('Keep in mind that multiple suggestions for the same word might be made \nif the first is not correct')
print('In these cases, enter \'N\' until the correct word is suggested')
print('Please open the file \'resultCheck\' to view the corrections that should be made')
print()

#run provided text file
test=Words('test.txt')
test.checkWords()

#explain where to find the updated text file
print()
print(f'The file with updated spellings is titled {test.newFile}')
print()

#set tracking variable for while loop
end=0

#ask if they would like to continue with new file
print('Would you like to check spelling of a new file?')
repeat=input('Type Y or N to respond: ')
if repeat.upper()=='N':
    end+=1

#if they reply Y continue
while end==0:
    print()
    #prompt user for new file
    userFile=input('Please enter a text file (be sure to include .txt): ')
    newFile=Words(userFile)
    newFile.checkWords()
    print()
    #inform user of updated file with new spellings
    print(f'The file with updated spellings is titled {newFile.newFile}')
    print()
    #ask if they would like to continue with new file
    print('Would you like to check spelling of a new file?')
    repeat=input('Type Y or N to respond: ')
    if repeat.upper()=='N':
        end+=1

    

