define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_decision_lists_new_form'

        var grid = w2ui ['voting_protocol_vote_decision_lists_grid']

        function change_val (name, value) {

            $('#' + name).val (value)
            $('#' + name).trigger ('change')

        }

        function recalc () {

            var tables = {'11.1': 'management_type_table',
                          '2.1': 'forming_fund_table'
            }

            var sizes = {'management_type_table': {form: 300,
                                                   page: 280,
                                                   box: 340,
                                                   popup: 350,
                                                   'form-box': 370},
                         'forming_fund_table':    {form: 300,
                                                   page: 280,
                                                   box: 340,
                                                   popup: 350,
                                                   'form-box': 370},
                         'decision_type_table':   {form: 260,
                                                   page: 240,
                                                   box: 300,
                                                   popup: 310,
                                                   'form-box': 330},
                        }

            function disable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (disable_element)
                $table.find('label').each (disable_element)
                $table.hide ()
            }

            function enable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (enable_element)
                $table.find('label').each (enable_element)
                $table.show ()
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

            table_name = 'decision_type_table'
            if (tables[v.decisiontype_vc_nsi_63])
                table_name = tables[v.decisiontype_vc_nsi_63]

            enable_block(table_name)

            change_val ('questionname', data.vc_nsi_63[v.decisiontype_vc_nsi_63])

            for (item in sizes[table_name]) {
                $('#' + table_name).closest ('.w2ui-' + item).height (sizes[table_name][item])
            }

        }

        $(view).w2popup('open', {

            width  : 580,
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