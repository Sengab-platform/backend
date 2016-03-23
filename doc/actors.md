# Table of Contents
1. [Receptionist](#1-receptionist)
 2. [Project Manager](#2-project-manager)
     3. [Project Validator](#3-project-validator)
     4. [Project Creator](#4-project-creator)
     5. [Details Retriever](#5-details-retriever)
     6. [Stats Retriever](#6-stats-retriever)
     7. [Results Retriever](#7-results-retriever)
  8. [User Manager](#8-user-manager)
     9. [User Retriever](#9-user-retriever)
     10. [Info Retriever](#10-info-retriever)
     11. [Projects Retriever](#11-projects-retriever)
     12. [Activity Retriever](#12-activity-retriever)
  13. [Contribution Manager](#13-contribution-manager)
     14. [Contribution Validator](#14-contribution-validator)
     15. [Contribution Creator](#15-contribution-creator)
 16. [Enrollment Manager](#16-enrollment-manager)
     17. [Enrollment Handler](#17-enrollment-handler)
     18. [Withdraw Manager](#18-withdraw-manager)
  19. [Category Manager](#19-category-manager)
     20. [Categories Retriever](#20-categories-retriever)
     21. [Category Projects Retriever](#21-category-projects-retriever)

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


## 10. Info Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
GetUserProfile(userId) |



## 11. Projects Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ListProjectsOfUer(userId, type,created, offset, limit) | type could be enrolled or created



## 12. Activity Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ListUserActivity(userId, offset, limit) |





## 13. Contribution Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
SubmitContribution(c: contribution) |

## 14. Contribution Validator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
ValidateContribution(c: contribution) |

## 15. Contribution Creator

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
CreateContribution(c: contribution) |


## 16. Enrollment Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
Enroll(projectId, userId) |
Withdraw(projectId, userId) |


## 17. Enrollment Handler

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
Enroll(projectId, userId) |


## 18. Withdraw Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
Withdraw(projectId, userId) |

## 19. Category Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
RetrieveCategories(offset, limit) |
RetrieveCategoryProjects(categoryId, offset, limit) |



## 20. Categories Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
RetrieveCategories(offset, limit) |




## 21. Category Projects Retriever

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
RetrieveCategoryProjects(categoryId, offset, limit) |
