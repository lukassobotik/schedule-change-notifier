# VŠEM Schedule Change Notifier
This is a **simple script** that notifies you when the **schedule changes** on the [**VŠEM website**](https://www.akademievsem.cz/).
Because of the schedule being changed **so often**, I decided to make a script that would notify **me or others** when the schedule **changes**.
It checks every **hour** to see if the schedule has changed, and if it has, it **emails you**.
The script will be running on a **server**, and the email will be sent to the email address **you send** the email to **subscribe** from.

## How to use
The script will email the schedule to the email address you send the email from. So send the email from the email address you want to receive the schedule at. But don't worry, the script will not send any other emails to that address.

[**Send an Email**](mailto:info@vsemschedulechangenotifier.tech) to the email address `info@vsemschedulechangenotifier.tech` with the subject `subscribe` to **subscribe** to the service.

[**Send an Email**](mailto:info@vsemschedulechangenotifier.tech) to the email address `info@vsemschedulechangenotifier.tech` with the subject `unsubscribe` to **unsubscribe** from the service.


## How to run the script yourself
If you want to run the script yourself, you can do so by following these steps:

### Prerequisites
- [**JDK 19**](https://www.oracle.com/java/technologies/downloads/#jdk19-linux) or **higher**
- Any **IDE** that supports **Java** (I recommend [**IntelliJ IDEA**](https://www.jetbrains.com/idea/download/))
- Any [**SMTP**](https://www.javatpoint.com/simple-mail-transfer-protocol) server. I recommend [**Mailtrap**](https://mailtrap.io/) or [**SendGrid**](https://sendgrid.com/)

### External Libraries
- [**JavaMail API**](https://javaee.github.io/javamail/) (included in the project)
- [**Jsoup**](https://jsoup.org/) (included in the project)
- [**Dotenv**](https://github.com/cdimascio/dotenv-java) (included in the project)
- [**Activation API**](https://www.oracle.com/java/technologies/java-beans-activation.html) (included in the project)

### Steps
1. Clone the repository
2. Create a file called `.env` in the src folder
3. Add the following to the `.env` file:
```dotenv
FROM_EMAIL=
PASSWORD=
POP_HOST=
SMTP_HOST=
POP_PORT=
SMTP_PORT=
```
4. Fill in the values for the variables straight after the `=`:
##### For Example:
```dotenv
FROM_EMAIL=your@email.com
```
- `FROM_EMAIL` - The email address you want to send the email from
- `PASSWORD` - The password for the email address you want to send the email from
- `POP_HOST` - The POP3 host
- `SMTP_HOST` - The SMTP host
- `POP_PORT` - The POP3 port
- `SMTP_PORT` - The SMTP port
5. Run the script
6. _(Optional)_ If you want to, you can modify the script to be on any schedule from any website you want. But you will have to change the code to do so.