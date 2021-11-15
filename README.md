# GoTriple_Hack21_Greek-6
## Repository for team-6 working on Greek language (GoTriple Hackathon 2021)


This repository contains the system created in the context of the GoTriple Hackathon 2021. The task was to map a list of Social Science and Humanities (SSH) terms in Greek language to their corresponding resources in the Subject Headings scheme of the Library of Congress (LCSH). We designed and implemented a pipeline using the LCSH search API. For the final output of our work we had three information scientists of our team curating the results.

## Input
As available input we had the following:
- A list of Greek terms in JSON (3.greek_multidiscipline_SSH.json)
- [EKT-UNESCO Thesaurus](https://www.semantics.gr/authorities/vocabularies/ekt-unesco/vocabulary-entries)

The list in JSON is actually an SSH terms subset of the EKT-UNESCO Thesaurus.


## Pipeline
![architecture](https://github.com/EKT/GoTriple_Hack21_Greek-6/Hackatho21_Pipeline.jpg) 