# Hospital Management

Welcome to this **Hospital Management** repository. This is a **Spring Boot** project built as a practice assignment. The code, while functional, **is not production-ready** and **may contain questionable or non-ideal implementations**. Part of the challenge is to **discover**, **review**, and **improve** these elements.

---

## What to Expect

- A **simple** REST API for managing `Patients` and their `Appointments`.
- Multiple classes (controllers, services, entities, and repositories).
- **Incomplete** or **inefficient** approaches to certain tasks.

---

## Glossary

Below are the primary entities you’ll find in this codebase:

1. **Patient**
    - Represents an individual in the hospital system.
    - Fields may include:
        - `id`: auto-generated primary key
        - `name`: name of the patient
        - `ssn`: Social Security Number (used here as a unique identifier)
        - `appointments`: a list of `Appointment` objects linked to this patient

2. **Appointment**
    - Represents a scheduled appointment or event for a patient.
    - Fields may include:
        - `id`: auto-generated primary key
        - `reason`: a textual reason for the appointment (e.g., “Checkup”)
        - `date`: the date of the appointment
        - `patient`: a reference to the `Patient` who owns this appointment

---

## Goals

1. **Explore the codebase**: Familiarize yourself with the structure and logic.
2. **Identify potential issues**: Think about security, performance, maintainability, design patterns, etc.
3. **Propose and/or implement improvements**: Refactor, rewrite, or reorganize parts of the code to showcase your approach.

---

## Results
the application is building without errors
the application is starting without errors
All the test pass successfully

### Fixes:
- No packages to organize the project -> created new packages to organize different components
- REST APIs now use Spring Boot validators.
- REST APIs now use the right method for DELETE.
- REST APIs now returns the right status code.
- All the models now have private variables for encapsulation.
- Used transactional in HospitalService methods to prevent lazy fetch from Hibernate outside the transaction, which is raising an exception, used also open-in-view=false which is covering the lazy fetch exception.
- Bring HospitalUtils thread safe using AtomicInteger and making it a Component for Spring instead of static, this is ensuring the singleton instance.
- for Appointment and Patient now the models use rightly equals and hashcode.
- Fixed N+1 query on appointment lookups using @EntityGraph (fetch join), verified with show-sql that it dropped from N+1 queries down to 1.
- Switched from field injection (@Autowired on fields) to constructor injection for testability.

### Patterns used:
- Basic GlobalExceptionHandler implemented to centralize main exceptions and return the right status code for REST APIs.
- Decoupled internal business logic model as appointment and patient from the object which REST APIs returns, decided to use record as a response to prevent mutable objects. 

### Testing:
- Added real tests at three levels: `@DataJpaTest` for repositories, Mockito for the service layer, `MockMvc` for controllers.
- Writing controller tests actually caught a real bug: `MethodArgumentNotValidException` wasn't handled, so failed validation returned 500 instead of 400.

### Style decisions:
Never logged the user SSN or exposed it in a response, because there is no need to expose sensible data of the users.

### Parts to improve:
- APIs are public, there is no security protocol to prevent ungranted access. one simple protocol can prevent unexpected access.
- Using Micrometer can be a good solution for tracking the usage of APIs instead of using HospitalUtils manually in case the traffic increase.
