Proof of concept for Spring Sleuth in microservices.

There are two services in a single application. A service that handles Mail Rooms, and a service 
that handles Stockrooms. To start each, use the spring profile: --spring.profiles.active=mailroom or 
--spring.profiles.active=stockroom. There are the standard REST services to list and add stockrooms and
mail rooms, and each service has a receive endpoint as well. The stockroom receive adds to an 
inventory list. The mail room end point forwards the request to the stockroom in the attn field.

Sleuth add trace id and span id. each service has a unique spand id per request. The trace id is forwarded
to all downstream service (mail room to stockroom). In addition, the mail room adds its id as the 
'userId' field to all downstream services.

Logging of all fields is done with JSON formatting.



