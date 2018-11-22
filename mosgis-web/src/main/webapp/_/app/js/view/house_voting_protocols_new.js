define ([], function () {

    return function (data, view) {

        var name = 'house_voting_protocols_new_form'
        
        function recalc () {

            $('.w2ui-form').height (250)
            $('.w2ui-page').height (230)
            $('.w2ui-box').height (290)
            $('.w2ui-popup').height (300)

            var tables = {'avoting_table': [], 'meeting_table': [], 'evoting_table': ['evoting_period_table'], 'meet_av_table': []}
            var sizes = {'avoting_table': {form: 330,
                                           page: 310,
                                           box: 370,
                                           popup: 380},
                         'meeting_table': {form: 330,
                                           page: 310,
                                           box: 370,
                                           popup: 380},
                         'evoting_table': {form: 365,
                                           page: 345,
                                           box: 385,
                                           popup: 415,
                                           'form-box': 400},
                         'meet_av_table': {form: 395,
                                           page: 375,
                                           box: 415,
                                           popup: 445,
                                           'form-box': 430}}

            function disable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (disable_element)
                $table.find('label').each (disable_element)
                $table.hide ()

                if (tables[table_name] != undefined) {
                    tables[table_name].forEach ((el, i, arr) => {
                        disable_block (el)
                    })
                }
            }

            function disable_element (i, element) {
                $(element).prop ('disabled', true)
            }

            function enable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (enable_element)
                $table.find('label').each (enable_element)
                $table.show ()

                if (tables[table_name] != undefined) {
                    tables[table_name].forEach ((el, i, arr) => {
                        enable_block (el)
                    })
                }

                for (item in sizes[table_name]) {
                    $table.closest ('.w2ui-' + item).height (sizes[table_name][item])
                }
            }

            function enable_element (i, element) {
                $(element).prop ('disabled', false)
            }

            for (var table in tables) {
                disable_block (table)
            }

            var v = w2ui [name].values ()

            //===========================================================

            enable_block (v.form_ + '_table')
        }
        
        $(view).w2popup('open', {

            width  : 620,
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
                    
                            {name: 'extravoting', type: 'list', options: { items: [
                                {id: 0, text: "Ежегодное"},
                                {id: 1, text: "Внеочередное"},
                            ]}},
                            {name: 'meetingeligibility', type: 'list', options: { items: [
                                {id: 0, text: "Правомочное"},
                                {id: 1, text: "Неправомочное"},
                            ]}},
                            
                            {name: 'form_', type: 'list', options: { items: data.vc_voting_forms.items }},
                            
                            {name: 'avotingdate', type: 'date'},
                            {name: 'resolutionplace', type: 'text'},

                            {name: 'meetingdate', type: 'datetime'},
                            {name: 'votingplace', type: 'text'},

                            {name: 'evotingdatebegin', type: 'datetime'},
                            {name: 'evotingdateend', type: 'datetime'},
                            {name: 'discipline', type: 'text'},
                            {name: 'inforeview', type: 'text'},

                            {name: 'meeting_av_date', type: 'datetime'},
                            {name: 'meeting_av_date_end', type: 'date'},
                            {name: 'meeting_av_place', type: 'text'},
                            {name: 'meeting_av_res_place', type: 'text'},
                        ],
                        
                        onChange: function (e) {if (e.target == "form_") e.done (recalc)},
                        
                        onRender: function (e) {e.done (setTimeout (recalc, 100))}

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_house_voting_protocols_new)

                }
                
            }
            
        });
    
    }
    
    
});