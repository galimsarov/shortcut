package com.example.core.a_solid;

import com.example.core.a_solid.e_dip.DipExample;
import com.example.core.a_solid.d_isp.IspExample;
import com.example.core.a_solid.c_lsp.LspExample;
import com.example.core.a_solid.b_ocp.OcpExample;
import com.example.core.a_solid.a_srp.SrpExample;

public class SolidDemoApp {
    public static void main(String[] args) {
        System.out.println("=== SOLID definitions ===");
        for (SolidDefinitions.Definition definition : SolidDefinitions.all()) {
            System.out.println("- " + definition.name());
            System.out.println("  Canonical: " + definition.canonical());
            System.out.println("  Simplified: " + definition.simplified());
        }

        System.out.println("\n=== SRP ===");
        SrpExample.demo();

        System.out.println("\n=== OCP ===");
        OcpExample.demo();

        System.out.println("\n=== LSP ===");
        LspExample.demo();

        System.out.println("\n=== ISP ===");
        IspExample.demo();

        System.out.println("\n=== DIP ===");
        DipExample.demo();
    }
}
