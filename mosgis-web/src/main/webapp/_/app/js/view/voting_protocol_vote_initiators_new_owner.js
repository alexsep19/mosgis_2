define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_initiators_new_owner_form'

        var grid = w2ui ['voting_protocol_vote_initiators_grid']

        var ids = []
        grid.records.forEach ((element, i, arr) => {
            if (element['uuid_ind'] != undefined)
                ids.push(element['uuid_ind'])
        })

        var owners = []
        data.owners.items.forEach ((element, i, arr) => {
            if (!ids.includes (element['id'])) owners.push (element)
        })

        if (owners.length == 0) { 
            if (ids.length == 0) {
                alert ('Список собственников пуст')
                return false;
            }
            else {
                alert ('Все собственники уже являются инициаторами')
                return false;
            }
        }

        $(view).w2popup('open', {

            width  : 500,
            height : 150,

            title   : 'Добавление инициатора - физического лица',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'voting_protocol_vote_initiators_new_owner_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'uuid_ind', type: 'list', options: {items: owners}},
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_vote_initiators_new_owner)

                }
                
            }
            
        });
    
    }
    
    
});