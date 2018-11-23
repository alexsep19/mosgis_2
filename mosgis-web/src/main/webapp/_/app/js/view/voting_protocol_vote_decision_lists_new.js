define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_decision_lists_new_form'

        var grid = w2ui ['voting_protocol_vote_decision_lists_grid']

        $(view).w2popup('open', {

            width  : 700,
            height : 400,

            title   : 'Новая повестка',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'voting_protocol_vote_decision_lists_new_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'decisiontype_vc_nsi_63', type: 'list', options: {items: data.vc_nsi_63.items}},
                            {name: 'questionname', type: 'text'},
                            {name: 'managementtype_vc_nsi_25', type: 'list', options: {items: data.vc_nsi_25.items}},
                            {name: 'formingfund_vc_nsi_241', type: 'list', options: {items: data.vc_nsi_241.items}},
                            {name: 'agree', type: 'text'},
                            {name: 'against', type: 'text'},
                            {name: 'abstent', type: 'text'},
                            {name: 'votingresume', type: 'list', options: {items: [
                                {id: 0, text: "Решение принято"},
                                {id: 1, text: "Решение не принято"},
                            ]}},
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_vote_decision_lists_new)

                }
                
            }
            
        });
    
    }
    
    
});