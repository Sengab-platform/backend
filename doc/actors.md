#Table of Content
- [Receptionist](#receptionist)
 - 1. [Project Manager](#1-project-manager)
     - [Project Validator](#1-project-validator)
       - [Project Creator](#-project-creator)
     - [Project Retriever](#2-project-retriever)
       - [Details Retriever](#-details-retriever)
       - [Stats Retriever](#-stats-retriever)
       - [Results Retriever](#-results-retriever)
  - 2. [User Manager](#2-user-manager)
      - [User Retriever](#1-user-retriever)
       - [User Projects Retriever](#-projects-retriever)
       - [Info Retriever](#-info-retriever)
       - [Activity Retriever](#-activity-retriever)
  - 3. [Contribution Manager](#3-contribution-manager)
       - [Contribution Validator](#1-contribution-validator)
        - [Contribution Creator](#-contribution-creator)
  - 4. [Enrollment Manager](#16-enrollment-manager)
      - [Enrollment Handler](#1-enrollment-handler)
      - [Withdraw Handler](#2-withdraw-handler)
  - 5. [Category Manager](#5-category-manager)
      - [Categories Retriever](#1-categories-retriever)
      - [Category Projects Retriever](#2-category-projects-retriever)


# Receptionist

## Description
This actor is the Interface of our Actor System and Play Action methods.

## Accept Messages

Message | Description
------- | -----------
msg : ProjectMessage | this is a trait which all Project Requests extend
msg : UserMessage | this is a trait which all User Requests extend
msg : ContributionMessage | this is a trait which all Contribution Requests extend
msg : EnrollmentMessage |  this is a trait which all Enrollment Requests extend
msg : CategoryMessage | this is a trait which all Category Requests extend

## 1. Project Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
CreateProject(p:Project,userID) |
ListProjects(filter, offset,limit) |
GetProjectDetails(projectID) |
GetProjectResults(projectID, offset, limit) |
GetProjectStats(projectID) |
SearchProjects(keyword) |

### 1. Project Validator

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
ValidateProject(p:Project,userID) |

<br>

#### &bull; Project Creator

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
CreateProject(p:Project,userID) |

<br>

### 2. Project Retriever

#### Description
TODO

#### Accept Messages

Message | Description
------- | -----------
GetProjectDetails(projectID) |
GetProjectResults(projectID, offset, limit) |
GetProjectStats(projectID) |

<br>

#### &bull; Details Retriever

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
ListProjects(filter, offset, limit) |
GetProjectDetails(projectID) |
SearchProjects(keyword) |

<br>

#### &bull; Stats Retriever

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
GetProjectStats(projectID) |

<br>

#### &bull; Results Retriever

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
GetProjectResults(projectID, offset, limit) |

<br><hr>

## 2. User Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
GetUserProfile(userID) |
ListUserActivity(userID, offset, limit) |
ListProjectsOfUser(userID, sort, offset, limit) | sort could be enrolled or created

### 1. User Retriever

#### Description
TODO

#### Accept Messages

Message | Description
------- | -----------
GetUserProfile(userID) |
ListUserActivity(userID, offset, limit) |
ListProjectsOfUser(userID, sort, offset, limit) | sort could be enrolled or created

<br>

#### &bull; User Projects Retriever

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
ListProjectsOfUser(userID, sort, offset, limit) | sort could be enrolled or created

<br>

#### &bull; Info Retriever

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
GetUserProfile(userID) |

<br>

#### &bull; Activity Retriever

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
ListUserActivity(userID, offset, limit) |

<br><hr>


## 3. Contribution Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
SubmitContribution(c: contribution,userID) |

### 1. Contribution Validator

#### Description
TODO

#### Accept Messages

Message | Description
------- | -----------
ValidateContribution(c: contribution,userID) |

<br>

#### &bull; Contribution Creator

##### Description
TODO

##### Accept Messages

Message | Description
------- | -----------
CreateContribution(c: contribution,userID) |

<br><hr>

## 4. Enrollment Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
Enroll(projectID, userID) |
Withdraw(projectID, userID) |


### 1. Enrollment Handler

#### Description
TODO

#### Accept Messages

Message | Description
------- | -----------
Enroll(projectID, userID) |


### 2. Withdraw Handler

#### Description
TODO

#### Accept Messages

Message | Description
------- | -----------
Withdraw(projectID, userID) |

<br><hr>

## 5. Category Manager

### Description
TODO

### Accept Messages

Message | Description
------- | -----------
RetrieveCategories(offset, limit) |
RetrieveCategoryProjects(categoryID, offset, limit) |


### 1. Categories Retriever

#### Description
TODO

#### Accept Messages

Message | Description
------- | -----------
RetrieveCategories(offset, limit) |


### 2. Category Projects Retriever

#### Description
TODO

#### Accept Messages

Message | Description
------- | -----------
RetrieveCategoryProjects(categoryID, offset, limit) |
