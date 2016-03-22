# Actors

## 1. Receptionist

### Description
This actor is the Interface of Our Actor System and Play Action methods.

### Accept Messages

Message | Description
------- | -----------
msg : ProjectMessage | this is a trait which all Project Requests extend
msg : UserMessage | this is a trait which all User Requests extend
msg : ContributionMessage | this is a trait which all Contribution Requests extend
msg : EnrollmentMessage |  this is a trait which all Enrollment Requests extend
msg : CategoryMessage | this is a trait which all Category Requests extend

## 2. Project Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
CreateProject(p:Project) |
ListProjects(filter,offset,limit) |
GetProjectDetails(projectID) |
GetProjectResults(projectID,offset,limit) |
GetProjectStats(projectID) |
SearchProjects(keyword) |

## 3. Project Validator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ValidateProject(p:Project) |

## 4. Project Creator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
CreateProject(p:Project) |

## 5. Details Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ListProjects(filter,offset,limit) |
GetProjectDetails(projectID) |
SearchProjects(keyword) |



## 6. Stats Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
GetProjectStats(projectID) |


## 7. Results Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
GetProjectResults(projectID) |


## 8. User Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
GetUserProfile(userId) |
ListUserActivity(userId, offset, limit) |
ListProjectsOfUser(userId, type, offset, limit) | type could be enrolled or created

## 9. User Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
GetUserProfile(userId) |
ListUserActivity(userId, offset, limit) |
ListProjectsOfUer(userId, type,created, offset, limit) | type could be enrolled or created


## 9. Info Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
GetUserProfile(userId) |



## 9. Projects Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ListProjectsOfUer(userId, type,created, offset, limit) | type could be enrolled or created



## 9. Activity Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ListUserActivity(userId, offset, limit) |





## 9. Contribution Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
SubmitContribution(c: contribution) |

## 10. Contribution Validator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ValidateContribution(c: contribution) |

## 11. Contribution Creator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
CreateContribution(c: contribution) |


## 11. Enrollment Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
Enroll(projectId,userId) |
Withdraw(projectId,userId) |


## 11. Enrollment Handler

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
Enroll(projectId,userId) |


## 11. Withdraw Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
Withdraw(projectId,userId) |

## 11. Category Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
RetrieveCategories(offset, limit)) |
RetrieveCategoryProjects(categoryId,offset, limit)) |



## 11. Categories Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
RetrieveCategories(offset, limit)) |




## 11. Category Projects Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
RetrieveCategoryProjects(categoryId,offset, limit)) |
