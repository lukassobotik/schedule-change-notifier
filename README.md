# Schedule Change Notifier
This is a **simple script** that notifies you when the **schedule changes** on a website.
Because of the schedule being changed **so often**, I decided to make a script that would notify **me or others** when the schedule **changes**.
It checks every **hour** to see if the schedule has changed, and if it has, it **emails you**.
The script was running on a **server**, and the email was sent to the email address **you send** the email to **subscribe** from.

## How to run the script yourself
If you want to run the script yourself, you can do so by following these steps:

### Prerequisites
- [**JDK 11**](https://www.oracle.com/java/technologies/downloads/) or **higher**
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
    LOG_DIRECTORY_PATH=
    DEBUG=
    FROM_EMAIL=
    PASSWORD=
    POP_HOST=
    SMTP_HOST=
    POP_PORT=
    SMTP_PORT=
    ```
4. Fill in the values for the variables straight after the `=`, for example:
    ```dotenv
    LOG_DIRECTORY_PATH=/home/user/logs
    ```
   - `LOG_DIRECTORY_PATH` is the path to the directory from root, where the logs will be stored. Make sure the directory exists.
   - `DEBUG` is a boolean value, that determines whether the script will log debug messages. Set it to `true` or `false`. I recommend setting it to `false` when running the script on a server.
   - `FROM_EMAIL` - The email address you want to send the email from
   - `PASSWORD` - The password for the email address you want to send the email from
   - `POP_HOST` - The POP3 host
   - `SMTP_HOST` - The SMTP host
   - `POP_PORT` - The POP3 port
   - `SMTP_PORT` - The SMTP port
5. Run the script and it should work. If it doesn't, check the logs in the directory you specified in the `.env` file. If it still doesn't work, create an issue on the [**GitHub repository**](https://github.com/PuckyEU/schedule-change-notifier/issues/new).
6. If everything works, compile the script into a `.jar` file. [**Here**](https://www.jetbrains.com/help/idea/compiling-applications.html) you can find documentation on how to do it in IntelliJ Idea. You can run the `.jar` file by running `java -jar <jar file name>` in the terminal.
7. If you want to run the script **automatically**, you can do so by using a **cron job**. If you don't know anything about cron jobs, I suggest reading [**this article**](https://www.cyberciti.biz/faq/how-do-i-add-jobs-to-cron-under-linux-or-unix-oses/). I have the cron job set up like this:
    ```cron
    0 * * * * /usr/bin/java -jar /path/to/jar/vsem-schedule-change-notifier.jar
    ```
   This will run the script every hour.
8. _(Optional)_ If you want to, you can modify the script to be on any schedule from any website you want. But you will have to change the code to do so.


<details>
<summary>

## [Discontinued] How to Use
    
</summary>

**The Program is not running on a server anymore, so this step will not work.** If you want to use the program, you have to [run it yourself](#how-to-run-the-script-yourself).
        
The script will email the schedule to the email address you send the email from. So send the email from the email address you want to receive the schedule at. But don't worry, the script will not send any other emails to that address.
[**Send an Email**](mailto:info@vsemschedulechangenotifier.tech) to the email address `info@vsemschedulechangenotifier.tech` with the subject `subscribe` to **subscribe** to the service or `unsubscribe` to **unsubscribe** from the service.

</details>
