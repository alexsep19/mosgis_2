define ([], function () {

    return function (data, view) {
        
        var it = data.item

        $('title').text (data.vc_nsi_79[it.code_nsi_79] + ' №' + it.number_ + ' от ' + dt_dmy (it.date_))
        
        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'overhaul_address_program_doc_common', caption: 'Общие'},
                            {id: 'overhaul_address_program_doc_files', caption: 'Файлы документа'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_overhaul_address_program_doc

                    }                

                },

            ],

            onRender: function (e) {
                clickActiveTab (this.get ('main').tabs, 'overhaul_address_program_doc.active_tab')
            },

        });
            
    }

})