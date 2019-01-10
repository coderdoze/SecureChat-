# SecureChat

### An end to end encrpted android chat application

### Features implemented
* Fetch all local connected IPs in subnet
* Sends messages with the help of socket programming
* Provide option for **AES** and **DES** encryption
* Transfer of key through **RSA** Algorithm
* Used SHA-512 as a hash function for data integrity

### Description

#### MainActivity
This activity contains two threads one for extracting all local IPs connected to network and in
main thread it extracts user's device IP and MAC address by WIFI manager and Dhcp client.
On selecting any user's IP ChatActivity is opened.

#### ChatActivity
* **Socket programming**
  * Created server and client socket working on port 9700(random) for communication and messages are sent through `DataOutputStream` 
and recieved by `DataInputStream`.
* **Message**
  * Created a serializable class Detail which contains AES or DES encrypted message, RSA encrypted key, Digital Signature and public key of user.
Instance of this class is send through output stream every time send button is pressed.
* **Encrytion and decryption algos**
  * Used MAC address of device to generate AES(128 bit) and DES(64 bit) keys and this key is used in AES or DES algo to encrypt message 
  and returns encrypted message(a byte array). Now this key is encryted with public key of reciever and finally put all these things in Detail class  object.
  * At the reciever end RSA encrypted key is decrypted with private key of reciever and this key is used to decrypt message.
* **Digital Signature**
  * Sender creates digital signature with its IP address by CreateSignature function and at reciever end it is verified and if it matches then only message is displayed to user.
  
