package com.supermartijn642.movingelevators.gui;

import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 10/02/2022 by SuperMartijn642
 */
public class SynchingTextFieldWidget extends TextFieldWidget {

    private final Supplier<String> actualText;
    private final Consumer<String> onChange;
    private String lastText;
    private final List<String> pastText = new LinkedList<>();

    public SynchingTextFieldWidget(int x, int y, int width, int maxLength, Supplier<String> actualText, Consumer<String> onChange){
        super(x, y, width, 11, actualText.get(), maxLength);
        this.actualText = actualText;
        this.onChange = onChange;
        this.lastText = this.getText();
    }

    @Override
    public void update(){
        super.update();

        String floorName = this.actualText.get();
        if(!floorName.equals(this.lastText)){
            if(floorName.equals(this.getText()))
                this.pastText.clear();
            else{
                int index = this.pastText.indexOf(floorName);
                if(index < 0){
                    this.setTextSuppressed(floorName);
                    this.cursorPosition = this.selectionPos = this.getText().length();
                    this.moveLineOffsetToCursor();
                }else
                    this.pastText.subList(0, index + 1).clear();
            }
            this.lastText = floorName;
        }
    }

    @Override
    protected void onTextChanged(String oldText, String newText){
        this.onChange.accept(newText);
        this.pastText.add(oldText);
    }
}
