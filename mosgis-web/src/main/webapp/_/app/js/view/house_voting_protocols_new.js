define ([], function () {

    return function (data, view) {
        
        $(view).w2popup('open', {

            width  : 500,
            height : 390,

            title   : 'Добавление протокола',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'house_voting_protocols_new_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [                                
                            {name: 'protocolnum', type: 'text'},
                            {name: 'protocoldate', type: 'date'},
                    
                            {name: 'extravoting', type: 'list', options: {items: [{id:0, text: "Ежегодное"}, {id:1, text: "Внеочередное"}]}},
                            {name: 'meetingeligibility', type: 'list', options: {items: [{id:0, text: "Правомочно"}, {id:1, text: "Неправомочно"}]}},
                            
                            {name: 'form_', type: 'radio'},
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_house_voting_protocols_new)

                }
                
            }
            
        });
    
    }
    
    
});