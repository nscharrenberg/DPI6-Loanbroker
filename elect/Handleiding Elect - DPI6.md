# Handleiding Elect - DPI6

## Elect Contents

Deze zip bevat de volgende onderdelen:

- demos [^demo-folder]
  - Demo 1 - Multiple clients[^demo1]
  - Demo 2 - Companies go offline [^demo2]
  - Demo 3 - receive later and broker offline [^demo3]
- Broker [^broker-folder]
- Companies [^company-folder]
  - sodexo [^sodexo-project]
  - amazon [^amazon-project]
  - microsoft [^microsoft-project]
  - google [^google-project]
- clients[^multiple-clients] 
  - jobseeker [^jobseeker-project]
  - jobseeker2 [^jobseeker-project]
- Handleiding [^handleiding]

[^demo1]: Laat de werking van de applicatie zien met meerdere clients die het systeem gebruiken.
[^demo2]: Laat de werking van de applicatie zien, waarbij het systeem nogsteeds functioneert als een company  	offline gaat en weer online komt.
[^demo3]: Laat de werking van de applicatie zien, waarbij het systeem nogsteeds functioneert als een bedrijf offline is als de broker probeert om een bericht te versturen. Daarnaast laat het ook zien dat het systeem nogsteeds functioneert nadat de broker offline is geweest.
[^jobseeker-project]: bevat het project voor een jobseeker
[^google-project]: Bevat het project voor google, met als sector IT
[^microsoft-project]: Bevat het project voor microsoft, met als sector IT
[^amazon-project]: Bevat het project voor amazon, met als sector Logistiek
[^sodexo-project]: Bevat het project voor sodexo, met als sector Horeca
[^company-folder]: Bevat verschillende company projecten
[^broker-folder]: Bevat het project voor de broker
[^demo-folder]: Bevat demo videos om de werking van de applicatie te laten zien
[^multiple-clients]: Er wordt gebruik gemaakt van meerdere client projecten. Dit omdat alle systemen hun eigen in-memory database bevatten voor het geval dat de applicatie afsluit. Hierbij wordt er dan ook aangetoont dat de system onafhankelijk van elkaar draaien, en elke client ook zijn eigen in-memory database gebruikt.
[^handleiding]: Is het document dat je op dit moment aan het lezen bent. Dit bevat algemene informatie over het elect systeem, hoe je het kan draaien en waar bepaalde systeem elementen zich bevinden.



## Installatie

### Installeer activeMQ

Volg de "[Getting Started](http://activemq.apache.org/getting-started.html)" van apache activeMQ en zorg dat activeMQ draait.

### Draaien van de systemen

1. Extract de content uit `elect.zip`. Zodat je de folder structuur hebt die eerder beschreven is.
2. Open de projecten in de IDE die je graag gebruikt e.g Intellij
3. Launch de applicatie via de Main class (Application) in com.nscharrenberg.elect.{package}[^{package}]
4. Je krijgt nu een gui te zien, waarmee je kan werken. Elk project heeft zijn eigen GUI.

[^{package}]: {package} staat voor de package die je runt. e.g jobseeker, jobseeker2, broker, amazon, microsoft, sodexo etc…



# Patterns

## Request-Reply

| Systeem   | Klassen       | Logic                        |
| --------- | ------------- | ---------------------------- |
| Jobseeker | ResumeReply   | ApplicationConnectionGateway |
|           | ResumeRequest | ApplicationReceiverGateway   |
|           |               | ApplicationSenderGateway     |
|           |               | ApplicationGateway           |
| Broker    | OfferReply    | ApplicationConnectionGateway |
|           | OfferRequest  | ApplicationReceiverGateway   |
|           | ResumeReply   | ApplicationSenderGateway     |
|           | ResumeRequest | ApplicationGateway           |
|           | ListLine      |                              |
| Company   | OfferReply    | ApplicationConnectionGateway |
|           | OfferRequest  | ApplicationReceiverGateway   |
|           |               | ApplicationSenderGateway     |
|           |               | ApplicationGateway           |



## Message Filter

| Systeem | Logic              |
| ------- | ------------------ |
| Broker  | CompanyList        |
|         | ApplicationGateway |



## Guaranteed Delivery

| Systeem   | Logic              |
| --------- | ------------------ |
| Broker    | MessageReader      |
|           | MessageWriter      |
|           | ApplicationGateway |
|           | Controller         |
| Jobseeker | MessageReader      |
|           | MessageWriter      |
|           | ApplicationGateway |
|           | Controller         |
| Company   | MessageReader      |
|           | MessageWriter      |
|           | ApplicationGateway |
|           | Controller         |

