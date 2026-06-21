/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.play.server.S02PacketChat;
import org.apache.commons.lang3.RandomUtils;

@ModuleInfo(label="KillSults", category=ModuleCategory.CLIENT)
public class KillSultsModule
extends Module {
    private static final String[] INSULTS = new String[]{"%s porfavor venha usar Advantage e ser melhor.", "%s doutor ele tem potencial? N\u00e3o.", "%s, morreu para um client de chatgpt.", "%s, confie no processo", "%s vem usar advantage mano", "%s, a persist\u00eancia \u00e9 o caminho do \u00eaxito", "%s passou vergonha", "%s estou surpreso que voc\u00ea conseguiu apertar o bot\u00e3o de jogar", "%s como voc\u00ea consegue jogar com o seu qi de menos de 30?", "%s voc\u00ea \u00e9 o tipo de pessoa que fica em terceiro lugar em um 1v1", "%s minecraft n\u00e3o \u00e9 pra todo mundo, volta para o fortnite", "%s voc\u00ea joga mais ou menos que deus te proteja", "%s Advantage > voc\u00ea", "obrigado pela rosa! %s", "%s, freaky ass nigg\u061ca", "obrigado pela rosa, %s", "%s, eu diria desinstalar, mas voc\u00ea provavelmente erraria isso tamb\u00e9m", "pare de respirar, seu idiot %s", "atchoo.. ops saiu uma meleca.. e \u00e9 o %s! ", "algu\u00e9m pode dar um len\u00e7o a esse menino, %s est\u00e1 quase a chorar", "isso foi uma #VictoryRoyale!, boa sorte na pr\u00f3xima vez, %s", "se o corpo \u00e9 70%% de \u00e1gua, como %s \u00e9 100%% sal????", "%s voc\u00ea sabe que os jogadores cegos tamb\u00e9m merecem uma chance, eu te apoio", "isso foi realmente uma jogada muito ruim %s", "%s voc\u00ea consegue ao menos acertar um player parado?", "ei %s, quem deixou a sua jaula aberta?!", "algu\u00e9m em 1940 esqueceu de botar g\u00e1s em voc\u00ea, %s", "%s: eu sou preto e isso \u00e9 um assalto", "%s, eu entendo porque os seus parentes abusaram de voc\u00ea", "um momento de sil\u00eancio para o %s", "%s, voc\u00ea \u00e9 a inspira\u00e7\u00e3o pro aborto", "%s, voc\u00ea realmente gosta assim tanto de morrer?", "se eu tivesse escolha entre %s e felipe neto, eu escolheria o felipe neto", "ei %s, o que o seu QI e as suas kills t\u00eam em comum? ambos s\u00e3o baixos pra caralho", "ei %s, quer umas dicas de PvP?", "%s por favor, seja t\u00f3xico comigo, eu gosto disso", "uau %s, voc\u00ea acabou de morrer em um jogo de legos", "%s \u00e9 a prova que Deus tem senso de humor", "estou surpreso que voc\u00ea tenha conseguido apertar o bot\u00e3o 'Instalar' %s", "%s -inf social credit", "%s deve usar flap", "%s voc\u00ea morreu na porra de um jogo quadrado", "%s gosta de anime", "%s Advantage > voc\u00ea", "%s ruim, voc\u00ea quase nem me bateu", "%s, sua \u00e1rvore geneal\u00f3gica deve ser um cacto", "%s alguns garotos foram abandonados ao nascer, mas voc\u00ea foi claramente jogado em uma parede", "obrigado pela kill de gra\u00e7a %s", "%s voc\u00ea est\u00e1 sequer tentando?", "%s Voc\u00ea. \u00e9. Pessimo.", "%s me adicione para que computadores possam falar sobre como voc\u00ea \u00e9 in\u00fatil", "%s: 'Staff! Staff! Me ajuda! Eu sou uma porcaria neste jogo e estou ficando bravo!'", "%s \u00e9 realmente t\u00e3o dif\u00edcil mirar em mim enquanto estou bhoppando ao seu redor?", "%s, Vape \u00e9 uma coisa legal que voc\u00ea deveria pesquisar sobre", "%s eu n\u00e3o estou usando reach, voc\u00ea s\u00f3 precisa clicar mais r\u00e1pido", "%s voc\u00ea tem que usar o bot\u00e3o esquerdo e direito do mouse neste jogo, caso tenha esquecido", "%s a quantidade de ping que voc\u00ea tem equivale \u00e1s suas c\u00e9lulas cerebrais", "%s ALT+F4 para remover o problema", "%s ALT+F4 para um easter egg muito oculto!!1111!1", "%s volte para o Fortnite, onde voc\u00ea pertence, seu degenerado de 5 anos de idade", "%s ketamine melhor do que voc\u00ea!1", "%s Advantage melhor do que voc\u00ea!1", "%s eu sou um verdadeiro gamer, e voc\u00ea acabou de ser morto!!", "%s como voc\u00ea \u00e9 ruim. estou perdendo c\u00e9lulas cerebrais s\u00f3 de te ver jogar", "%s pule do pr\u00e9dio de sua escola com uma corda ao redor do pesco\u00e7o", "%s n\u00e3o, voc\u00ea n\u00e3o \u00e9 cego! Eu TE MATEI!", "%s L de lixo", "%s \u00e9 quase como se eu pudesse ouvir voc\u00ea gritar do outro lado", "a contagem de cromossomos em %s duplica o tamanho deste jogo", "um milh\u00e3o de anos de evolu\u00e7\u00e3o e temos pessoas como o %s", "%s deu ragequit", "%s, eu jogo fortnite com sua m\u00e3e", "%s bate com for\u00e7a, mas o pai dele o bate com mais for\u00e7a", "%s desista de viver", "como voc\u00ea apertou o bot\u00e3o DOWNLOAD com essa mira? %s", "eu diria que sua mira \u00e9 c\u00e2ncer, mas pelo menos o c\u00e2ncer mata pessoas %s", "%s \u00e9 quase t\u00e3o \u00fatil quanto pedais em uma cadeira de rodas", "a Mira do %s agora \u00e9 patrocinada pelo Parkinson!", "%s Por favor, voc\u00ea n\u00e3o poderia se comprometer a n\u00e3o morrer, senhor, obrigado", "%s voc\u00ea provavelmente chupa as ma\u00e7anetas das portas", "%s pare de respirar seu burro", "%s :batata:", "%s Super Mario Bros. som da morte", "%s knock knock, FBI abra a porta, vimos voc\u00ea procurar por vape cracked", "%s por favor pule da janela por vip de gra\u00e7a", "%s voc\u00ea nem sequer teve chance!", "e o %s continua tentando!", "eu n\u00e3o sabia que morrer era uma habilidade especial %s", "%s, Stephen Hawking tinha melhor coordena\u00e7\u00e3o motora do que voc\u00ea", "%s usa /login senha!11!1", "%s lol GG!!!", "%s N\u00c3O jogue mais no mush.", "%s gg guys obrigado pela minha primeira kill!", "n\u00e3o se esque\u00e7a de me reportar %s", "seu QI \u00e9 o de um Steve %s", "%s 2 mais 2 \u00e9 4, menos 1 esse \u00e9 o seu QI", "acho que voc\u00ea precisa de vape %s!", "%s, meu av\u00f4 cego com parkinson tem uma mira melhor do que voc\u00ea", "o fortnite est\u00e1 perdendo uma estrela. volte para o seu lugar, %s", "%s, estou perdendo pontos de QI s\u00f3 de v\u00ea-lo jogar", "os preservativos deveriam ter te pago pela campanha de marketing, %s", "%s, seus pais o abandonaram, e o orfanato fez o mesmo", "uma salva de palmas para %s, que n\u00e3o para de tentar", "%s por favor considere n\u00e3o viver", "%s \u00e9 o tipo de pessoa de assassinar algu\u00e9m e pedir desculpas dizendo que foi um acidente", "%s, obteve um F no teste de qi", "%s, crian\u00e7as como voc\u00ea foram a inspiration para o aborto", "%s seus dentes s\u00e3o como estrelas - dourados, e separados", "rosas s\u00e3o azuis, violetas s\u00e3o vermelhas, %s acabou de morrer", "%s eu n\u00e3o uso hack porque o CRIS ANTICHEAT est\u00e1 vigiando ent\u00e3o ele me baniria", "%s voc\u00ea morreu para o melhor hack do jogo, agora com bypass infinito de sprint!", "%s ja passa o zap da sua mae pra eu ser seu pai e te ensinar a jogar", "%s Voc\u00ea \u00e9 t\u00e3o ruim que o Minecraft deveria te banir do jogo"};
    private static final String[] DEATH_REGEX_TEMPLATES = new String[]{"^MORTE Voc\u00ea matou (.+)([\\w]+)\\.$", "^MORTE Voc\u00ea matou (.+)([\\w]+)\\.$", "^(.+) foi morto por %s\\(\\)\\.", "^%s derrubou (.+) de muito alto!", "^(.+) foi jogado no void por %s\\.", "^(.+) morreu no void para %s\\.(?: KILL FINAL!)?$", "^(.+) caiu no void por %s\\.(?: KILL FINAL!)?$", "^(.+) morreu para %s\\.(?: KILL FINAL!)?$"};
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat chatPacket = (S02PacketChat)event.getPacket();
            String text = chatPacket.getChatComponent().getUnformattedText();
            String playerName = Util.mc.thePlayer.getName();
            for (String template : DEATH_REGEX_TEMPLATES) {
                String regex = template.replace("%s", playerName);
                Matcher matcher = Pattern.compile(regex).matcher(text);
                if (!matcher.find()) continue;
                String victim = matcher.group(1);
                if (victim == null || victim.contains(" ")) break;
                String insult = INSULTS[RandomUtils.nextInt(0, INSULTS.length)];
                Util.mc.thePlayer.sendChatMessage(String.format(insult, victim));
                break;
            }
        }
    };
}

