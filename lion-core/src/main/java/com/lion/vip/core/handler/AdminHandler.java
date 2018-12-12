package com.lion.vip.core.handler;

import com.lion.vip.common.handler.BaseMessageHandler;
import com.lion.vip.core.LionServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.spi.OptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理器实现类
 */
@ChannelHandler.Sharable
public class AdminHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminHandler.class);
    private static String EOL = "\r\n";
    private final LocalDateTime startTime = LocalDateTime.now();
    private final Map<String, OptionHandler> optionHandlerMap = new HashMap<>();
    private final OptionHandler unsupported_handler = (_1, _2) -> "unsupported option";
    private final LionServer lionServer;

    public AdminHandler(LionServer lionServer) {
        this.lionServer = lionServer;
        init();
    }

    public void init() {
        register("help", (ctx, args) ->
                "Option                               Description" + EOL +
                        "------                               -----------" + EOL +
                        "help                                 show help" + EOL +
                        "quit                                 exit console mode" + EOL +
                        "shutdown                             stop lion server" + EOL +
                        "restart                              restart lion server" + EOL +
                        "zk:<redis, cs ,gs>                   query zk node" + EOL +
                        "count:<conn, online>                 count conn num or online user count" + EOL +
                        "route:<uid>                          show user route info" + EOL +
                        "push:<uid>, <msg>                    push test msg to client" + EOL +
                        "conf:[key]                           show config info" + EOL +
                        "monitor:[mxBean]                     show system monitor" + EOL +
                        "profile:<1,0>                        enable/disable profile" + EOL
        );

        register("quit", (ctx, args) -> "have a good day!");

        register("shutdown", (ctx, args) -> {
            new Thread(() -> System.exit(0)).start();
            return "try close connect server...";
        });

        register("count", (ctx, args) -> {
            switch (args) {
                case "conn":
                    return lionServer.getConnectionServer().getConnectionManager().getConnNum();
                case "online": {
                    return lionServer.getRouterCenter().getUserEventConsumer().getUserManager().getOnlineUserNum();
                }

            }
            return "[" + args + "] unsupported, try help.";
        });

        register("route", (ctx, args) -> {
            if (Strings.isNullOrEmpty(args)) return "please input userId";
            Set<RemoteRouter> routers = lionServer.getRouterCenter().getRemoteRouterManager().lookupAll(args);
            if (routers.isEmpty()) return "user [" + args + "] offline now.";
            return Jsons.toJson(routers);
        });

        register("conf", (ctx, args) -> {
            if (Strings.isNullOrEmpty(args)) {
                return CC.cfg.root().render(ConfigRenderOptions.concise().setFormatted(true));
            }
            if (CC.cfg.hasPath(args)) {
                return CC.cfg.getAnyRef(args).toString();
            }
            return "key [" + args + "] not find in config";
        });

        register("profile", (ctx, args) -> {
            if (args == null || "0".equals(args)) {
                Profiler.enable(false);
                return "Profiler disabled";
            } else {
                Profiler.enable(true);
                return "Profiler enabled";
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }
}
