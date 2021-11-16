# GoTriple_Hack21_Greek-6
## Repository for team-6 working on Greek language (GoTriple Hackathon 2021)


This repository contains the system created in the context of the GoTriple Hackathon 2021. The task was to map a list of Social Science and Humanities (SSH) terms in Greek language to their corresponding resources in the Subject Headings scheme of the Library of Congress (LCSH). We designed and implemented a pipeline using the LCSH search API. For the final output of our work we had three information scientists of our team curating the results.

## Input
As available input we had the following:
- A list of Greek terms in JSON (3.greek_multidiscipline_SSH.json)
- [EKT-UNESCO Thesaurus](https://www.semantics.gr/authorities/vocabularies/ekt-unesco/vocabulary-entries)

The list in JSON is actually an SSH terms subset of the EKT-UNESCO Thesaurus.


## Pipeline
![architecture](https://github.com/EKT/GoTriple_Hack21_Greek-6/blob/master//Hackatho21_Pipeline.jpg) 
The system takes as input the JSON with the greek terms. It iterates them one-by-one and gets their corresponding resource in the Thesaurus. Even though there is an [API](https://www.semantics.gr/authorities/swagger-ui.html) for the Thesaurus we prefered to download it in XML format and keep it in our disk instead. For every greek term that is to be mapped to the LCSH the system sends a search request to the API of LC. For example, for the term "Festivals" it sends the HTTP request
```shell
https://id.loc.gov/search/?q=Festivals&q=scheme:http://id.loc.gov/authorities/subjects&format=atom
```
which is the XML result of the search: 
```shell
https://id.loc.gov/search/?q=Festivals&q=scheme:http://id.loc.gov/authorities/subjects
```
These search requests return LCSH labels that are potential mappings to our terms. To be confident for the output we leave the final decision to the curators. To help them take decisions about the final mappings, for each Greek term we write in an XLSX file the 5 first results that came up (and are most likely the most similar). For each one of them we write its URI in LC and its path in the taxonomy. We find the paths in the taxonomy by recursively going to their parents until we get to the root. In LC there can be more than one parent; we simply take the first one encountered.
