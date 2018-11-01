package pfe.terrain.gen;

import pfe.terrain.gen.algo.constraints.Contract;
import pfe.terrain.gen.exception.MissingRequiredException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContractStore {
    List<Contract> contracts;

    public ContractStore(List<Contract> contracts){
        this.contracts = new ArrayList<>(contracts);
    }

    public List<Contract> getContracts() {
        return new ArrayList<>(contracts);
    }

    /**
     * @return all the required element by the stored contract
     */
    public Set<String> getAllRequired(){
        Set<String> required = new HashSet<>();

        for(Contract dependency : contracts){
            required.addAll(dependency.getContract().getRequired());
        }

        return required;
    }

    /**
     * @return all the created element by the stored contract
     */
    public Set<String> getAllCreated(){
        Set<String> created = new HashSet<>();

        for(Contract dependency : contracts){
            created.addAll(dependency.getContract().getCreated());
        }

        return created;
    }

    /**
     * add the conract to the list
     * @param contract to be add
     */
    public void add(Contract contract){
        contracts.add(contract);
    }

    /**
     * look for the contract providing the given element
     * @param creation element to find in the stored contract
     * @return the contract providing the desired element
     * @throws MissingRequiredException if the element is not provided by the stored contract
     */
    public Contract getContractCreating(String creation) throws MissingRequiredException{
        for(Contract contract : contracts){
            if(contract.getContract().getCreated().contains(creation)){
                return contract;
            }
        }
        throw new MissingRequiredException();
    }
}
