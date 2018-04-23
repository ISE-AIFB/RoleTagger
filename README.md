# RoleTagger:
This repository contains implementation of the Temporal Role Tagger. Most existing NERD applications are not able to detect or resolve ambiguities that require the determination of a temporal context for their correct interpretation, such as in temporal roles like *CEO of a company*, *Soccer World Champion*, or *head of a country*, i.e. descriptions of the position or purpose of specific named entities in specific situations that change over time. 

The aim of this project is detection temporal roles in a given sentence. 

![](https://github.com/ISE-AIFB/RoleTagger/blob/master/images/1.png "Temporal Role Tagger")

## Components:
The introduced pipeline here consists of multiple components as follows:
![](https://github.com/ISE-AIFB/RoleTagger/blob/master/images/2.png "Pipeline")

## Manually Curated Ground Truth: 
The text fragments were manually selected from the New York Times news corpus version 2008 as well as from English Wikipedia articles and comprise 178 positive and negative sentences.
Each role has been annotated with two tages: `<RP Categoty='CAT'>` for role phrases and `<HR>` for head roles.
`<HR>` identifies the most general variant of a role with potential high ambiguity, and the `<RP Categoty='CAT'>` further specifying the role with sufficient details for successful disambiguation, as e.g. in "the acting President of the U.S." the term "President" is the head role, while the entire phrase constitutes the role phrase including further specifications:
`<RP Categoty='HEAD_OF_STATE_TAG'>`the acting `<HR>`President`</HR>` of the U.S.`</RP>`
In this project `CAT` could be:
1. HEAD_OF_STATE_TAG
2. POPE_TAG
3. MONARCH_TAG
4. CHAIR_PERSON_TAG

## Preprocessing
Before able to run the code, few steps should be done. 
1. Downloading Standford CoreNLP pre-trained models anding putting them in the `/src/main/resources` folder 
2. Downloading Wikipedia dump file and running [wikiextractor](https://github.com/attardi/wikiextractor) by `-l` flag to preserving the links. After running extractor, move the files to the `wikipediafiles` folder in the project root directory.
3. Download `article_categories_en.ttl` from DBpedia download portal. Create a folder named `category` in the project root directory and move the file there.
4. Download `skos_categories_en.ttl` from DBpedia download portal and move it to the `category` in the project root directory.
5. Download `redirectPage` from [this link](https://www.dropbox.com/s/plfyiz2cz7jgrgb/redirectPage?dl=0) and move it to `requirements` folder in the project root directory

## How to run the code
**Running inside Eclipse**
This project is based on [Gradle](https://gradle.org/). So it could be easily imported to Eclipse. For importing it the Eclipse should contain [Buildship Plugin](https://projects.eclipse.org/projects/tools.buildship).  After installing [Buildship Plugin](https://projects.eclipse.org/projects/tools.buildship), you can easily import the project into the Eclipse as a Gradle project.

**Running in Terminal**
For running the project in terminal, first the project should be built. Run `./gradlew installDist`. Then you only need to  modify `config.properties` in `build/install/RoleTagger/bin` and finally run `build/install/RoleTagger/bin/RoleTagger.bat` or `build/install/RoleTagger/bin/RoleTagger.sh`.

**Modifying Pipeline**
To be able to en/dis able the components in the pipeline, the `PipeLine` class can be modified.
