# Actors

## 1. Receptionist

### Description
This actor is the Interface of Our Actor System and Play Action methods.

### Accept Messages

Message | Description
------- | -----------
msg : ProjectMessage | this is a trait which all Project Requests extends
msg : UserMessage | this is a trait which all User Requests extends
msg : ContributionMessage | this is a trait which all Contribution Requests extends
msg : EnrollmentMessage |  this is a trait which all Enrollment Requests extends
msg : CategoryMessage | this is a trait which all Category Requests extends

## 2. Project Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : ValidateProject | 
msg : RetrieveProject |

## 3. Project Validator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : CreateProject | 

## 4. Project Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : RetrieveResults | 
msg : RetrieveStats |
msg : RetrieveDetails |

## 5. User Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : RetrieveInfo | 
msg : RetrieveProjects | 
msg : RetrieveActivities | 

## 6. Contribution Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : ValidateContribution | 

## 7. Contribution Creator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : CreateContribution | 

## 8. Enrollment Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : Enroll |
msg : Withdraw |

## 9. Category Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
msg : RetrieveCategories |
msg : RetrieveCategoryProjects |




