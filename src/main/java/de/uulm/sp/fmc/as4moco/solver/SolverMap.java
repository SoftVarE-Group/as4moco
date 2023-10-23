package de.uulm.sp.fmc.as4moco.solver;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SolverMap {

    private final static Map<String, SolverInterface> solvers = Map.ofEntries(
            //todo add solvers
            Map.entry("c2d/default", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/c2d/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_default";
                }

            }),
            Map.entry("d4/default.sh", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/d4/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_default.sh";
                }

            }),
            Map.entry("dpmcpre/1pre1mp0", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/dpmcpre/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_1pre1mp0";
                }

                @Override
                public List<String> getParameters(File cnf) {
                    List<String> list = new ArrayList<>();
                    list.add("--outdir /tmp/"+ ThreadLocalRandom.current().nextInt());
                    list.add(cnf.getAbsolutePath());
                    return list;
                }
            }),
            Map.entry("dpmcpre/1pre1mp1", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/dpmcpre/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_1pre1mp1";
                }

                @Override
                public List<String> getParameters(File cnf) {
                    List<String> list = new ArrayList<>();
                    list.add("--outdir /tmp/"+ ThreadLocalRandom.current().nextInt());
                    list.add(cnf.getAbsolutePath());
                    return list;
                }
            }),
            Map.entry("gpmc/track1", new SolverInterface() { //TODO Missing Solver gpmc_r2/track1
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/gpmc/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_track1";
                }
            }),
            Map.entry("Narsimha-track1v-7112ef8eb466e9475/track1_conf1.sh", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/Narsimha-track1v-7112ef8eb466e9475/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_track1_conf1.sh";
                }
            }),
            Map.entry("Narsimha-track1v-7112ef8eb466e9475/track1_conf2.sh", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/Narsimha-track1v-7112ef8eb466e9475/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_track1_conf2.sh";
                }
            }),
            Map.entry("SharpSAT-TD-unweighted/default", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/SharpSAT-TD-unweighted/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_default";
                }
            }),
            Map.entry("TwG/1.sh", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/TwG/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_1.sh";
                }
            }),
            Map.entry("TwG/2.sh", new SolverInterface() {
                @Override
                public File getFolder() {
                    return new File("workingSolvers/MC2022_Solvers/Track1_MC/TwG/bin");
                }

                @Override
                public String getExecutable() {
                    return "starexec_run_2.sh";
                }
            })
            //TODO missing solver: Narsimha-track1v-51fd045537919d 1&2, ExactMC, gpmc_r2, mtmc/default
    );

    public static SolverInterface getSolver(String name){
        return solvers.get(name);
    }



}