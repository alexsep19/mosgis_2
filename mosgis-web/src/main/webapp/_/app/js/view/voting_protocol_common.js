define ([], function () {
    
    var form_name = 'voting_protocol_common_form'

    var read_only = false

    return function (data, view) {
    
        function recalc () {

            function disable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (disable_element)
                $table.find('label').each (disable_element)
                $table.hide ()
            }

            function disable_element (i, element) {
                $(element).prop ('disabled', true)
            }

            function enable_block (table_name) {
                var $table = $('#' + table_name)
                $table.find('input').each (enable_element)
                $table.find('label').each (enable_element)
                $table.show ()
            }

            function enable_element (i, element) {
                if (read_only != undefined && !read_only) {
                    $(element).prop ('disabled', false)
                }
            }

            tables = ['avoting_table', 'meeting_table', 'evoting_table', 'meet_av_table']
            sizes = [332, 366, 468, 400]

            tables.forEach (function (element, i, arr) {
                disable_block (element)
            })

            var v = w2ui [form_name].values ()

            enable_block (tables[v.form_])
            $('#layout_passport_layout_panel_top').height (sizes[v.form_]) // верх
            $('#layout_passport_layout_panel_main').css('top', sizes[v.form_] + 1 + 'px') // логи
        }

        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            $.each (w2ui [form_name].fields, function () {

                if (this.type != 'date') return
                
                var dt = r [this.name]
                var t_name = this.name.replace('date', 'time')

                if (dt != undefined && r [t_name] == undefined) {
                    r [t_name] = dt.substring (dt.indexOf (" ") + 1, dt.indexOf("."))
                }
                
                if (dt != undefined && dt.charAt (3) != '.') r [this.name] = dt_dmy (dt)
                        
            })

            w2ui [form_name].record = r
            
            $('div[data-block-name=voting_protocol_common] input').prop ({disabled: data.__read_only})

            read_only = data.__read_only

            w2ui [form_name].refresh ()

            recalc (data.__read_only)

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 400},
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