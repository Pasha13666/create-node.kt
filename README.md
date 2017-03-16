
# Create-node.kt

Create-node is simple system to downloading node.kt modules.

## Installing

Download file `create-node.sh`, add it to your `$PATH` and make executable:
```sh
wget "https://raw.githubusercontent.com/Pasha13666/create-node.sh/master/create-node.sh"
sudo mv create-node.sh /usr/bin
sudo chmod +x /usr/bin/create-node.sh
```

## Usage

```
Usage: create-node [ARGS]...
Where ARGS is:
    -?, -h, -help, --help       Show this help.
    -d=DIR, -directory=DIR      Create node in DIR, not in current directory.
    -S=URL, -server=URL         Use URL as base server url to download files.
    -m=MODULE, -module=MODULE   Use MODULE. This option can be repeated.
    -v, -verbose                Make more output.
    -V, -version                Print version and exit.
```

## FAQ

1. Error `Cant find java! Check your jvm installation and set \$JAVA_HOME.`
    - Install java.
    - Set `$JAVA_HOME` to your java installation path.
2. Error `Cannot find writable temporary directory! Try 'chmod +w .'.`
    - Run `sudo chmod +w .` or
    - Run `sudo chmod +w /tmp` or
    - Set `$HOME` to your home directory.
3. Error `Cant find wget or curl! Please, install it.`
    - Install wget or
    - Install curl.
4. Error `Error downloading file %URL%! Try to download it and place to %PATH%.`
    - Check your internet connection.
    - Download file from %URL% to %PATH%.
5. Error `Invalid module name: %MODULE%!`
    - Check your `-module` options.

