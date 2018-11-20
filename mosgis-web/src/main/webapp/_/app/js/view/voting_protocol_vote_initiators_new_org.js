define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_initiators_new_org_form'

        var grid = w2ui ['voting_protocol_vote_initiators_grid']

        var ids = []
        grid.records.forEach ((element, i, arr) => {ids.push(element['uuid_org'])})

        var orgs = []
        data.vc_orgs.items.forEach ((element, i, arr) => {
            if (!ids.includes (element['id'])) orgs.push (element)
        })

        if (orgs.length == 0) { 
            alert ('Все организации уже являются инициаторами')
            return false;
        }

        $(view).w2popup('open', {

            width  : 500,
            height : 150,

            title   : 'Добавление инициатора - организации',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'voting_protocol_vote_initiators_new_org_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'uuid_org', type: 'list', options: {items: orgs}},
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_vote_initiators_new_org)

                }
                
            }
            
        });
    
    }
    
    
});