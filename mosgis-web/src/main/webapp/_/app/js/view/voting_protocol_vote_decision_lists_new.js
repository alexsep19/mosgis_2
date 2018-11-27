define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_decision_lists_new_form'

        var grid = w2ui ['voting_protocol_vote_decision_lists_grid']

        function recalc () {

            var tables = {'11.1': 'management_type_table', 
                          '2.1': 'forming_fund_table'
            }

            function disable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (disable_element)
                $table.find('label').each (disable_element)
            }

            function enable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (enable_element)
                $table.find('label').each (enable_element)
            }

            function disable_element (i, element) {
                $(element).prop ('disabled', true)
            }

            function enable_element (i, element) {
                $(element).prop ('disabled', false)
            }

            for (var table in tables) {
                disable_block (tables[table])
            }

            var v = w2ui [name].values ()

            enable_block(tables[v.decisiontype_vc_nsi_63])

        }

        $(view).w2popup('open', {

            width  : 650,
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
                            {name: 'questionname', type: 'textarea'},
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

                        onChange: function (e) {if (e.target == "decisiontype_vc_nsi_63") e.done (recalc)},
                        
                        onRender: function (e) {e.done (setTimeout (recalc, 100))}

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_vote_decision_lists_new)

                }
                
            }
            
        });
    
    }
    
    
});