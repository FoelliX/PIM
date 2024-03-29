![java 17](https://img.shields.io/badge/java-17-brightgreen.svg)
---
<p align="center">
	<br />
	<font style="color: #FFFFFF; font-size: 50px; background:#336633; border: 25px solid #336633; border-radius: 10px;">PIM</font>
</p>

# PIM
The *Precise Intent Matcher (PIM)* can be used as an alternative or a replacement for the CONNECT operator of the [AQL](https://foellix.github.io/AQL-System).
It connects Intent-Sinks with Intent-Sources and thereby detects ICC and IAC flows between one or more apps.
To do so, it builds up a client-server connection with an Android device or emulator.
Through this connection PIM request the construction of Intents and Intent-Filters based on the provided input (Intent-Sinks & -Sources).
Whenever the Android system installed on the device or emulator decides, that an Intent's action-category-data triple matches the one of an Intent-Filter, a connection between the associated Intent-Sink and -Source is derived.
Accordingly PIM takes multiple AQL-Answers as input and outputs a single one.

## Example
The goal of the following query, for example, is to find any flows inside and between `A` and `B`.

```
MATCH [
	Flows IN App(’A.apk’) ?,
	IntentSources IN App(’A.apk’) ?,
	IntentSinks IN App(’A.apk’) ?,
	Flows IN App(’B.apk’) ?,
	IntentSources IN App(’B.apk’) ?,
	IntentSinks IN App(’B.apk’) ?
]
```

The `MATCH` operator refers to PIM.
The questions in its scope may be answered by different tools such as FlowDroid or IC3.
A complete and fully described example can be found in the referenced paper (see [Publications](#Publications)).

## Configuration and Launch Parameters
When PIM is executed for the first time, you are asked to setup PIM unless you have defined the following parameters in the `config.properties` file beforehand:

| Entry | Meaning |
| ----- | ------- |
| `sdkPath=/path/to/Android/Android/sdk` | Path to the Android SDK. The Android development bridge located in this directory will be called. |
| `deviceName=emulatorToBeUsed` | Specify the name of the emulator or device you want to execute PIM on. |
| `rebootAllowed=false` | Determines if PIM has the permission to restart the selected device. |
| `appRelocationPath=/search/path` | Provide a directory where the analyzed apps may be found. For example, the upload directory of an AQL-WebService. |

The following launch parameters can be provided while running PIM:

| Parameter | Meaning |
| --------- | ------- |
| `start` | Starts the selected emulator or device and the PIM-Server on it. |
| `stop` | Stops the selected emulator or device. |
| `ask %ANSWER% [, %ANSWER%]*` | All provided AQL-Answers (%ANSWER%) will be matched by PIM. At least one answer must be specified. (If the PIM-Server is not started, it will be started and stopped once the execution finishes.) |

### PIM together with IC3
IC3 can be used to get information about IntentSinks and IntentSources.
PIM is able to accurately connect these to inter-component flows.
To fully use this feature, PIM's underlying [AQL-System](https://FoelliX.github.io/AQL-System) should be setup (Tutorial: [Configuration](https://github.com/FoelliX/AQL-System/wiki/Configuration)).
It should be setup to use a taint analysis tool such as [FlowDroid](https://github.com/secure-software-engineering/FlowDroid).
This tool should be ran with the sources and sinks in the following file [SourcesAndSinks_intent.txt](https://github.com/FoelliX/PIM/blob/master/SourcesAndSinks_intent.txt).
This is required to find additional IntentSources that share the same action-category-data triples as those found by IC3.
The AQL-System's configuration should look similar to the following one and be stored in PIM's root directory under the name ``windows.xml`` or ``linux.xml`` (depending on your operating system):
```xml
<config>
	<tools>
		<tool name="FlowDroid" version="X" external="false">
			<priority>1</priority>
			<execute>
				<run>/path/to/FlowDroid/script.sh/.bat that uses SourcesAndSinks_intent.txt</run>
				<result>/path/to/FlowDroid/results/%APP_APK_FILENAME%_result.txt</result>
				<instances>0</instances>
				<memoryPerInstance>8</memoryPerInstance>
			</execute>
			<path>/path/to/FlowDroid</path>
			<questions>IntraAppFlows</questions>
		</tool>
	</tools>
</config>
```

## Publications
- *Together Strong: Cooperative Android App Analysis* (Felix Pauck, Heike Wehrheim)  
ESEC/FSE 2019 [https://dl.acm.org/citation.cfm?id=3338915](https://dl.acm.org/citation.cfm?id=3338915)

## License
PIM is licensed under the *GNU General Public License v3* (see [LICENSE](https://github.com/FoelliX/PIM/blob/master/LICENSE)).

## Contact
**Felix Pauck** (FoelliX)  
Paderborn University  
fpauck@mail.uni-paderborn.de  
[http://www.FelixPauck.de](http://www.FelixPauck.de)

## Links
- PIM is employed in CoDiDroid: [https://github.com/FoelliX/CoDiDroid](https://github.com/FoelliX/CoDiDroid)