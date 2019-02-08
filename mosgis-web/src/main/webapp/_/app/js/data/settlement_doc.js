define ([], function () {

    $_DO.choose_tab_settlement_doc = function (e) {

        var name = e.tab.id

        var layout = w2ui ['topmost_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('settlement_doc.active_tab', name)

        use.block (name)

    }

    return function (done) {

            query ({type: 'settlement_docs'}, {}, function (data) {

                add_vocabularies (data, {
                    vc_gis_status: 1
                })

                var it = data.item

                it.last_annul = data.last_annul

                it._can = {}

                var is_locked = it.is_deleted || (it.uuid_org_author != $_USER.uuid_org)

                if (!is_locked) {

                    switch (it.id_sd_status) {
                        case 10:
                        case 14:
                            it._can.delete = 1
                    }

                    switch (it.id_sd_status) {
                        case 10:
                        case 11:
                            it._can.edit = 1
                            it._can.approve = 1
                    }

                    switch (it.id_sd_status) {
                        case 14:
                        case 34:
                        case 40:
                            it._can.alter = 1
                    }

                    switch (it.id_sd_status) {
                        case 40:
                            it._can.annul = 1
                    }

                }

                $('body').data ('data', data)

                done (data)

            })


    }

})