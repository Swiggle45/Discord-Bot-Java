import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class Bot {
	
	interface Command {
		Mono<Void> execute(MessageCreateEvent event);
	}
	
	private static final String token = Token.getToken();
	
	private static final Map<String, Command> commands = new HashMap<>();
	
	static {
			commands.put("ping", event -> event.getMessage().getChannel()
				.flatMap(channel -> channel.createMessage("Pong!"))
				.then());
			commands.put("hi", event -> event.getMessage().getChannel()
				.flatMap(channel -> channel.createMessage("Hi!"))
				.then());
			commands.put("hello", event -> event.getMessage().getChannel()
				.flatMap(channel -> channel.createMessage("Hello!"))
				.then());
			commands.put("roll", event -> event.getMessage().getChannel()
				.flatMap(channel -> channel.createMessage(rollDiceHelper(event.getMessage().getContent())))
				.then());
		}

	public static void main(String[] args) {
		GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();
		
		client.getEventDispatcher().on(ReadyEvent.class)
			.subscribe(event -> {
				User self = event.getSelf();
				System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
			});
		
		Mono.just("Hello World!").subscribe(System.out::println);
		
		
		client.getEventDispatcher().on(MessageCreateEvent.class)
			.filter(messageEvent -> messageEvent.getMessage().getAuthor().map(user -> !user.isBot()).orElse(false))
			.flatMap(event -> Mono.just(event.getMessage().getContent().toLowerCase())
			.flatMap(content -> Flux.fromIterable(commands.entrySet()) 
			.filter(entry -> content.startsWith(entry.getKey()))
			.flatMap(entry -> entry.getValue().execute(event))
			.next()))
			.subscribe();
		
		
		
		
		
		
		client.onDisconnect().block();
	}
	
	private static String rollDiceHelper(String s) {
		try {
			String answer = s;
			Random rand = new Random();
			String sub = s.substring(s.indexOf('d') + 1);
			if (sub.contains("shannon")) {
				answer = "You rolled an ";
				int a = rand.nextInt(7);
				switch(a) {
				case 0: return answer + "s";
				case 1: return answer + "h";
				case 2: return answer + "a";
				case 3: return answer + "n";
				case 4: return answer + "n";
				case 5: return answer + "o";
				case 6: return answer + "n";
				}
			}
			List<String> list = new ArrayList<String>();
			String[] split = sub.split("[^0-9]");
			for (int i=0; i < split.length; i++) list.add(split[i]);
			for (String a : list) {
				if (a.isEmpty()) list.remove(a);
			}
			if (list.isEmpty()) return "type \"roll d\" followed by the max number on the dice";
			int num = Integer.parseInt(list.get(0));
			answer = "" + (rand.nextInt(num) + 1);
			return "You rolled a " + answer;
		} catch (Exception e) {
			return "type \"roll d\" followed by the max number on the dice";
		}
	}
	
	

}
