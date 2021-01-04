# Cartographer

A program for reading and parsing SRG and MCP mappings.

## Requirements

 * **JDK 15**, as it uses the Records preview feature.

## Usage
  1. Download a MCPConfig package and extract it into a `config` folder. _This provides the SRG names._
  2. Download a MCP mapping package and extract it into a `mcp` folder. _This provides the MCP mappings._
 
     The folder structure should be:
     ```
     (.) 
     + config/
     |  + access.txt
     |  + constructors.txt
     |  + exceptions.txt
     |  + joined.tsrg
     |  + static_methods.txt
     + mcp/
         + fields.csv
         + methods.csv
         + params.csv
     ```
  3. Run Cartographer.
     It will read & parse the files, then store the SRG names and IDs into a `srg_database.txt` file.
     This file will be parsed and used by future runs, instead of re-parsing the MCP package. 


## License
This project is under the MIT License. See `LICENSE.txt` for the full license text.