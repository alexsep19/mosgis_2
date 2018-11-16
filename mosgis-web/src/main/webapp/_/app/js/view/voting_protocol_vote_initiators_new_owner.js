define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_initiators_new_owner_form'
        
        $(view).w2popup('open', {

            width  : 300,
            height : 300,

            title   : 'Добавление инициатора - физического лица',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'voting_protocol_vote_initiators_new_owner_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'uuid_ind', type: 'list', voc: data.prop_doc},
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_vote_initiators_new_owner)

                }
                
            }
            
        });
    
    }
    
    
});