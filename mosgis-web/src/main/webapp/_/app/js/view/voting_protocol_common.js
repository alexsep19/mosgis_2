define ([], function () {
    
    var form_name = 'voting_protocol_common_form'

    return function (data, view) {
    
        function recalc (hidden) {
            function disabling (element, i, arr) {
                element.val ('').prop ('disabled', true)
                element.closest ('.w2ui-field').hide ()
            }
            
            function enabling (element, i, arr) {
                element.closest ('.w2ui-field').show ()
                element.val ('').prop ('disabled', false)
            }

            var items = {'avoting': [$('#avotingdate'),
                                     $('#resolutionplace')],
                         'meeting': [$('#meetingdate'),
                                     $('#meetingtime'),
                                     $('#votingplace')],
                         'evoting': [$('#evotingdatebegin'),
                                     $('#evotingtimebegin'),
                                     $('#evotingdateend'),
                                     $('#evotingtimeend'),
                                     $('#discipline'),
                                     $('#inforeview')],
                         'meet_av': [$('#meeting_av_date'),
                                     $('#meeting_av_time'),
                                     $('#meeting_av_date_end'),
                                     $('#meeting_av_res_place')]}

            items['avoting'].forEach(disabling);
            items['meeting'].forEach(disabling);
            items['evoting'].forEach(disabling);
            items['meet_av'].forEach(disabling);

            var v = w2ui [form_name].values ()

            switch (v.form_) {
                case 0:
                    items['avoting'].forEach(enabling);
                    break;
                case 1:
                    items['meeting'].forEach(enabling);
                    break;
                case 2:
                    items['evoting'].forEach(enabling);
                    break;
                case 3:
                    items['meet_av'].forEach(enabling);
                    break;
            }         
        }

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            $.each (w2ui [form_name].fields, function () {

                if (this.type != 'date') return
                
                var dt = r [this.name]

                if (dt != undefined) {
                    r [this.name.replace('date', 'time')] = dt.substring (dt.indexOf (" ") + 1, dt.indexOf("."))
                }
                
                if (dt != undefined && dt.charAt (3) != '.') r [this.name] = dt_dmy (dt)
                        
            })

            console.log (data)
            
            w2ui [form_name].record = r

            //recalc ()
            
            $('div[data-block-name=voting_protocol_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 300},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'voting_protocol_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_voting_protocol_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))

        fill (view, data.item, $panel)

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                                
                {name: 'protocolnum', type: 'text'},
                {name: 'protocoldate', type: 'date'},
        
                {name: 'extravoting', type: 'list', options: { items: [
                    {id: 0, text: "Ежегодное"},
                    {id: 1, text: "Внеочередное"},
                ]}},
                {name: 'meetingeligibility', type: 'list', options: { items: [
                    {id: "C", text: "Правомочное"},
                    {id: "N", text: "Неправомочное"},
                ]}},
                
                {name: 'form_', type: 'list', options: { items: [
                    {id: 0, text: "Заочное голосование (опросным путем)"},
                    {id: 1, text: "Очное голосование"},
                    {id: 2, text: "Заочное голосование с использованием системы"},
                    {id: 3, text: "Очно-заочное голосование"},
                ]}},
                
                {name: 'avotingdate', type: 'date'},
                {name: 'resolutionplace', type: 'text'},
                {name: 'meetingdate', type: 'date'},
                {name: 'meetingtime', type: 'time'},
                {name: 'votingplace', type: 'text'},
                {name: 'evotingdatebegin', type: 'date'},
                {name: 'evotingtimebegin', type: 'time'},
                {name: 'evotingdateend', type: 'date'},
                {name: 'evotingtimeend', type: 'time'},
                {name: 'discipline', type: 'text'},
                {name: 'inforeview', type: 'text'},
                {name: 'meeting_av_date', type: 'date'},
                {name: 'meeting_av_time', type: 'time'},
                {name: 'meeting_av_date_end', type: 'date'},
                {name: 'meeting_av_res_place', type: 'text'},
            ],

            onChange: function (e) {if (e.target == "form_") e.done (recalc)},
                        
            onRender: function (e) {e.done (setTimeout (recalc, 100))},
            
        })

        $_F5 (data)     

    }
    
})