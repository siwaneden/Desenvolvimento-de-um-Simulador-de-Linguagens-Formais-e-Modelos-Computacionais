import unittest

from python_backend.algoritmos.complexidade import analisar_complexidade
from python_backend.algoritmos.linguagens import analisar_linguagem
from python_backend.algoritmos.turing import simular_maquina


class LinguagensTest(unittest.TestCase):
    def test_operacoes_basicas(self):
        resultado = analisar_linguagem(["a", "b"], "abba", ["abba", "a"], "ba", 2)
        self.assertTrue(resultado["cadeia_valida"])
        self.assertTrue(resultado["pertence_linguagem"])
        self.assertEqual(resultado["tamanho"], 4)
        self.assertEqual(resultado["concatenacao"], "abbaba")
        self.assertEqual(resultado["sigma_estrela"], ["ε", "a", "b", "aa", "ab", "ba", "bb"])


class TuringTest(unittest.TestCase):
    def test_reconhece_copia(self):
        self.assertTrue(simular_maquina("copia", "010#010")["aceita"])
        self.assertFalse(simular_maquina("copia", "010#011")["aceita"])

    def test_soma_unaria(self):
        resultado = simular_maquina("soma", "000100")
        self.assertTrue(resultado["aceita"])
        self.assertEqual(resultado["saida"], "00000")


class ComplexidadeTest(unittest.TestCase):
    def test_estimativa_quadratica(self):
        resultado = analisar_complexidade("turing_copia", 10)
        self.assertEqual(resultado["operacoes_estimadas"], 100)
        self.assertEqual(resultado["pior"], "O(n²)")
        self.assertIn("funcionamento", resultado["analise"])
        self.assertIn("quadrático", resultado["analise"]["justificativa"])


if __name__ == "__main__":
    unittest.main()
